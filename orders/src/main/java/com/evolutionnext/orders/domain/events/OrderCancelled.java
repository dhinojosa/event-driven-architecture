package com.evolutionnext.orders.domain.events;


import com.evolutionnext.orders.domain.aggregate.order.Order;

public record OrderCancelled(Order order, String reason) implements OrderEvent {
}
