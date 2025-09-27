package com.evolutionnext.orders.domain.events;


import com.evolutionnext.orders.domain.aggregate.order.Order;

public record OrderCreated(Order order) implements OrderEvent{
}
