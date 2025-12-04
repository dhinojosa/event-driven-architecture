package com.evolutionnext.order.domain.events;


import com.evolutionnext.order.domain.aggregate.order.Order;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.order.OrderItem;

public sealed interface OrderEvent permits OrderEvent.OrderCancelled, OrderEvent.OrderCreated, OrderEvent.OrderItemAdded, OrderEvent.OrderPlaced {
    record OrderCancelled(OrderId orderId, String reason) implements OrderEvent {
    }
    record OrderPlaced(Order order) implements OrderEvent {
    }
    record OrderItemAdded(OrderItem orderItem) implements OrderEvent{}

    record OrderCreated(Order order) implements OrderEvent{}
}
