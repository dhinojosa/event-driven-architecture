package com.evolutionnext.order.infrastructure.adapter.out;


import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.domain.aggregate.order.Order;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.order.OrderItem;
import com.evolutionnext.order.domain.aggregate.product.ProductId;
import com.evolutionnext.order.domain.events.OrderEvent;
import com.evolutionnext.order.events.*;
import com.evolutionnext.order.port.out.OrderRepository;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecordBase;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

public class PostgresOrderRepository implements OrderRepository {
    private final DataSource dataSource;

    public PostgresOrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static byte[] toAvroBytes(SpecificRecordBase record) {
        try {
            KafkaAvroSerializer serializer = new KafkaAvroSerializer();
            serializer.configure(
                Map.of("schema.registry.url", "http://localhost:8081"),
                false
            );
            return serializer.serialize("orders", record);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to convert Avro to bytes", e);
        }
    }

    @Override
    public void persist(Order order) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (var orderStmt = connection.prepareStatement(
                "INSERT INTO \"order\" (orderid, customerid) VALUES (?, ?)")) {
                orderStmt.setObject(1, order.getOrderId().id());
                orderStmt.setObject(2, order.getCustomerId().uuid());
                orderStmt.executeUpdate();

                try (var itemStmt = connection.prepareStatement(
                    "INSERT INTO orderitem (orderid, productid, quantity, price) VALUES (?, ?, ?, ?)")) {
                    for (var item : order.getOrderItems()) {
                        itemStmt.setObject(1, order.getOrderId().id());
                        itemStmt.setObject(2, item.productId().id());
                        itemStmt.setInt(3, item.quantity());
                        itemStmt.setBigDecimal(4, item.price());
                        itemStmt.executeUpdate();
                    }
                }

                order.events().stream().flatMap(orderEvent ->
                        createMessage(orderEvent).stream())
                    .forEach(orderEventMessage -> {
                        try {
                            sendToOutbox(connection, orderEventMessage);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to persist order", e);
        }
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        try (Connection connection = dataSource.getConnection()) {
            try (var orderStmt = connection.prepareStatement(
                """
                    SELECT o.orderid, o.customerid, i.productid, i.quantity, i.price
                    FROM "order" o LEFT JOIN orderitem i ON o.orderid = i.orderid
                    WHERE o.orderid = ?
                    """)) {
                orderStmt.setObject(1, orderId.id());
                var rs = orderStmt.executeQuery();
                if (!rs.next()) return Optional.empty();

                UUID customerId = (UUID) rs.getObject("customerid");
                List<OrderItem> items = new ArrayList<>();
                do {
                    if (rs.getObject("productid") != null) {
                        items.add(new OrderItem(
                            new ProductId((UUID) rs.getObject("productid")),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price")
                        ));
                    }
                } while (rs.next());

                Order value = new Order(orderId, new CustomerId(customerId), items);
                return Optional.of(value);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order", e);
        }
    }

    @Override
    public List<Order> findAll() {
        try (Connection connection = dataSource.getConnection()) {
            try (var stmt = connection.prepareStatement(
                "SELECT o.orderid, o.customerid, i.productid, i.quantity, i.price " +
                "FROM \"order\" o LEFT JOIN orderitem i ON o.orderid = i.orderid")) {
                var rs = stmt.executeQuery();

                Map<UUID, List<OrderItem>> orderItems = new HashMap<>();
                Map<UUID, Order> orders = new HashMap<>();
                while (rs.next()) {
                    UUID orderId = (UUID) rs.getObject("orderid");
                    UUID customerId = (UUID) rs.getObject("customerid");

                    orders.computeIfAbsent(orderId, id -> new Order(new OrderId(id),
                        new CustomerId(customerId)));

                    if (rs.getObject("productid") != null) {
                        orderItems.computeIfAbsent(orderId, k -> new ArrayList<>())
                            .add(new OrderItem(
                                new ProductId((UUID) rs.getObject("productid")),
                                rs.getInt("quantity"),
                                rs.getBigDecimal("price")
                            ));
                    }
                }

                for (var entry : orderItems.entrySet()) {
                    Order order = orders.get(entry.getKey());
                    entry.getValue().forEach(order::addOrderItem);
                }

                return new ArrayList<>(orders.values());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all orders", e);
        }
    }

    private Optional<OrderEventMessage> createMessage(OrderEvent orderEvent) {
        return switch(orderEvent) {
            case OrderEvent.OrderCancelled(var orderId, String reason) ->
                Optional.of(new OrderEventMessage(orderId.id(),
                    Instant.now(),
                    EventType.ORDER_CANCELLED,
                    new OrderCancelledMessage(reason)));
            case OrderEvent.OrderPlaced(var o)  ->
                Optional.of(new OrderEventMessage(o.getOrderId().id(),
                    Instant.now(),
                    EventType.ORDER_PLACED,
                    new OrderPlacedMessage(
                        new OrderMessage(o.getCustomerId().uuid(),
                            o.getOrderItems()
                                .stream()
                                .map(oi ->
                                    new OrderItemMessage(oi.productId().id(),
                                        oi.quantity(),
                                        oi.price().doubleValue())).toList()
                        ))));
            default -> Optional.empty();
        };
    }


    @Override
    public void update(Order o) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (var orderStmt = connection.prepareStatement(
                    "UPDATE \"order\" SET customerid = ? WHERE orderid = ?")) {
                    orderStmt.setObject(1, o.getCustomerId().uuid());
                    orderStmt.setObject(2, o.getOrderId().id());
                    orderStmt.executeUpdate();
                }

                try (var deleteStmt = connection.prepareStatement(
                    "DELETE FROM orderitem WHERE orderid = ?")) {
                    deleteStmt.setObject(1, o.getOrderId().id());
                    deleteStmt.executeUpdate();
                }

                try (var itemStmt = connection.prepareStatement(
                    "INSERT INTO orderitem (orderid, productid, quantity, price) VALUES (?, ?, ?, ?)")) {
                    for (var item : o.getOrderItems()) {
                        itemStmt.setObject(1, o.getOrderId().id());
                        itemStmt.setObject(2, item.productId().id());
                        itemStmt.setInt(3, item.quantity());
                        itemStmt.setBigDecimal(4, item.price());
                        itemStmt.executeUpdate();
                    }
                }

                o.events().stream().flatMap(orderEvent ->
                    createMessage(orderEvent).stream())
                    .forEach(orderEventMessage -> {
                        try {
                            sendToOutbox(connection, orderEventMessage);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order", e);
        }
    }

    private static void sendToOutbox(Connection connection, OrderEventMessage orderEventMessage) throws SQLException {
        String outboxSql = "INSERT INTO order_outbox (id, aggregatetype, aggregateid, type, payload) VALUES (?, ?, ?, ?, ?)";
        try (var outboxStmt = connection.prepareStatement(outboxSql)) {
            outboxStmt.setObject(1, UUID.randomUUID());
            outboxStmt.setString(2, "Order");
            outboxStmt.setString(3, orderEventMessage.getOrderId().toString());
            outboxStmt.setString(4, orderEventMessage.getEventType().toString());
            outboxStmt.setBytes(5, toAvroBytes(orderEventMessage));
            outboxStmt.executeUpdate();
        }
    }
}
