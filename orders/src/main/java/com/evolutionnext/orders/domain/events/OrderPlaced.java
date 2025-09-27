package com.evolutionnext.orders.domain.events;


import com.evolutionnext.orders.domain.aggregate.order.Order;

public record OrderPlaced(Order order) implements OrderEvent {
}
