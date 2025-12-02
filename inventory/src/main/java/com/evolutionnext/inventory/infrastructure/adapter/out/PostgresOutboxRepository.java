package com.evolutionnext.inventory.infrastructure.adapter.out;

import com.evolutionnext.inventory.domain.aggregate.Product;
import com.evolutionnext.inventory.domain.aggregate.ProductId;
import com.evolutionnext.inventory.events.InventoryEventMessage;
import com.evolutionnext.inventory.events.ProductCreatedMessage;
import com.evolutionnext.inventory.port.out.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.evolutionnext.inventory.events.EventType.PRODUCT_CREATED;

public class PostgresOutboxRepository implements ProductRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresOutboxRepository.class);

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public PostgresOutboxRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        this.objectMapper = new ObjectMapper();
    }

    public static byte[] toAvroBytes(SpecificRecordBase record) {
        try {
            KafkaAvroSerializer serializer = new KafkaAvroSerializer();
            serializer.configure(
                Map.of("schema.registry.url", "http://schema-registry:8081"),
                false
            );
            return serializer.serialize("inventory", record);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Avro to bytes", e);
        }
    }

    public static String toJson(SpecificRecordBase record) {
        try {

            Schema schema = record.getSchema();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Encoder jsonEncoder = EncoderFactory.get().jsonEncoder(schema, out);
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(schema);

            writer.write(record, jsonEncoder);
            jsonEncoder.flush();

            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Avro to JSON", e);
        }
    }

    @Override
    public void persist(Product product) {
        InventoryEventMessage inventoryEventMessage = new InventoryEventMessage(
            product.productId().id(), Instant.now(), PRODUCT_CREATED,
            new ProductCreatedMessage(product.name(), product.description(), product.price().doubleValue(), product.stock())
        );

        logger.info("Persisting avro: {}", toJson(inventoryEventMessage));

        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String productSql = "INSERT INTO product (productid, name, description, price, stock) VALUES (?, ?, ?, ?, ?)";
                try (var productStmt = connection.prepareStatement(productSql)) {
                    productStmt.setObject(1, product.productId().id(), java.sql.Types.OTHER);
                    productStmt.setString(2, product.name());
                    productStmt.setString(3, product.description());
                    productStmt.setBigDecimal(4, product.price());
                    productStmt.setInt(5, product.stock());
                    productStmt.executeUpdate();
                }

                String outboxSql = "INSERT INTO product_outbox (id, aggregatetype, aggregateid, type, payload) VALUES (?, ?, ?, ?, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {
                    outboxStmt.setObject(1, java.util.UUID.randomUUID());
                    outboxStmt.setString(2, "Product");
                    outboxStmt.setString(3, product.productId().id().toString());
                    outboxStmt.setString(4, PRODUCT_CREATED.toString());
                    outboxStmt.setBytes(5, toAvroBytes(inventoryEventMessage));
                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error with database connection", e);
        }
    }

    @Override
    public void updatePrice(ProductId productId, BigDecimal price) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String productSql = "UPDATE product SET price = ? WHERE productid = ?";
                try (var productStmt = connection.prepareStatement(productSql)) {
                    productStmt.setBigDecimal(1, price);
                    productStmt.setObject(2, productId.id());
                    productStmt.executeUpdate();
                }

                String outboxSql = "INSERT INTO product_outbox (product_id, event_type, event_name, event, timestamp) VALUES (?, ?, ?, ?::jsonb, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {

                    // 1. Specific Payload
                    ObjectNode specificEventPayload = objectMapper.createObjectNode()
                            .put("price", price.doubleValue()); // Avro double

                    // 2. Union Wrapper
                    ObjectNode unionWrapper = objectMapper.createObjectNode();
                    unionWrapper.set("com.evolutionnext.inventory.events.PriceChangedMessage", specificEventPayload);

                    // 3. Root Envelope
                    ObjectNode rootEnvelope = objectMapper.createObjectNode();
                    rootEnvelope.put("productId", productId.id().toString());
                    rootEnvelope.put("timestamp", System.currentTimeMillis());
                    rootEnvelope.put("eventType", "PRICE_CHANGED");
                    rootEnvelope.set("event", unionWrapper);

                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("jsonb");
                    jsonObject.setValue(rootEnvelope.toString());

                    outboxStmt.setObject(1, productId.id());
                    outboxStmt.setString(2, "PRICE_CHANGED");
                    outboxStmt.setString(3, "PriceChangedMessage");
                    outboxStmt.setObject(4, jsonObject);
                    outboxStmt.setLong(5, System.currentTimeMillis());

                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Error updating price and outbox", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error with database connection", e);
        }
    }

    @Override
    public Product findById(ProductId productId) {
        String sql = "SELECT * FROM product WHERE productid = ?";
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, productId.id());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Product(
                    new ProductId(resultSet.getObject("productid", java.util.UUID.class)),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getBigDecimal("price"),
                    resultSet.getInt("stock")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product", e);
        }
    }

    @Override
    public void updateStock(ProductId productId, int stock) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String productSql = "UPDATE product SET stock = ? WHERE productid = ?";
                try (var productStmt = connection.prepareStatement(productSql)) {
                    productStmt.setInt(1, stock);
                    productStmt.setObject(2, productId.id());
                    productStmt.executeUpdate();
                }

                String outboxSql = "INSERT INTO product_outbox (product_id, event_type, event_name, event, timestamp) VALUES (?, ?, ?, ?::jsonb, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {

                    // 1. Specific Payload
                    ObjectNode specificEventPayload = objectMapper.createObjectNode()
                            .put("stock", stock);

                    // 2. Union Wrapper
                    ObjectNode unionWrapper = objectMapper.createObjectNode();
                    unionWrapper.set("com.evolutionnext.inventory.events.StockChangedMessage", specificEventPayload);

                    // 3. Root Envelope
                    ObjectNode rootEnvelope = objectMapper.createObjectNode();
                    rootEnvelope.put("productId", productId.id().toString());
                    rootEnvelope.put("timestamp", System.currentTimeMillis());
                    rootEnvelope.put("eventType", "STOCK_CHANGED");
                    rootEnvelope.set("event", unionWrapper);

                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("jsonb");
                    jsonObject.setValue(rootEnvelope.toString());

                    outboxStmt.setObject(1, productId.id());
                    outboxStmt.setString(2, "STOCK_CHANGED");
                    outboxStmt.setString(3, "StockChangedMessage");
                    outboxStmt.setObject(4, jsonObject);
                    outboxStmt.setLong(5, System.currentTimeMillis());

                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Error updating stock and outbox", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error with database connection", e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM product";
        List<Product> products = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
             var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                products.add(new Product(
                    new ProductId(resultSet.getObject("productid", java.util.UUID.class)),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getBigDecimal("price"),
                    resultSet.getInt("stock")
                ));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all products", e);
        }
    }
}
