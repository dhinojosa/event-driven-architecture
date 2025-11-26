package com.evolutionnext.order.domain.events;


import com.evolutionnext.order.domain.aggregate.order.Order;

public record OrderCreated(Order order) implements OrderEvent{
}
