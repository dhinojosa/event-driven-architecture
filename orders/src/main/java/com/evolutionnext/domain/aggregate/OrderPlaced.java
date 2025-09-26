package com.evolutionnext.domain.aggregate;


public record OrderPlaced(Order order) implements OrderEvent {
}
