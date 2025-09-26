package com.evolutionnext.domain.aggregate;


public record OrderCancelled(Order order, String reason) implements OrderEvent {
}
