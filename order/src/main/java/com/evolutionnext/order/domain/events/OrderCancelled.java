package com.evolutionnext.order.domain.events;


import com.evolutionnext.order.domain.aggregate.order.Order;

public record OrderCancelled(Order order, String reason) implements OrderEvent {
}
