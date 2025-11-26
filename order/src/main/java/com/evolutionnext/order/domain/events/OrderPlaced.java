package com.evolutionnext.order.domain.events;


import com.evolutionnext.order.domain.aggregate.order.Order;

public record OrderPlaced(Order order) implements OrderEvent {
}
