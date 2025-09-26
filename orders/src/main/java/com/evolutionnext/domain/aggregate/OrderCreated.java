package com.evolutionnext.domain.aggregate;


public record OrderCreated(Order order) implements OrderEvent{
}
