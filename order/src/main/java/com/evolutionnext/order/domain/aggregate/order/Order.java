package com.evolutionnext.order.domain.aggregate.order;

import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.domain.events.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class Order {
    private static final Logger logger = LoggerFactory.getLogger(Order.class);

    private final OrderId orderId;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final List<OrderEvent> events = new ArrayList<>();

    public Order(OrderId orderId, CustomerId customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = OrderStatus.CREATED;
        this.items = new ArrayList<>();
    }

    public void placeOrder() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order cannot be placed in its current state");
        }
        this.status = OrderStatus.PLACED;
        this.events.add(new OrderEvent.OrderPlaced(this));
    }

    public void addOrderItem(OrderItem orderItem) {
        logger.info("Received order item: {}", orderItem);

        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items to an order that is not in the NEW state");
        }
        this.items.add(orderItem);
        this.events.add(new OrderEvent.OrderItemAdded(orderItem));
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
        this.events.add(new OrderEvent.OrderCancelled(orderId, "Order cancelled by customer"));
    }

    public static Order create(OrderId orderId, CustomerId customerId, String state) {
        return new Order(orderId, customerId);
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public BigDecimal getTotal() {
        return items.stream().map(OrderItem::price).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
            .add("orderId='" + orderId + "'")
            .add("status=" + status)
            .add("items=" + items)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId) && status == order.status && Objects.equals(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, status, items);
    }

    public List<OrderEvent> events() {
        return Collections.unmodifiableList(events);
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(items);
    }

    public void clearEvents() {
        events.clear();
    }
}
