package com.evolutionnext.order.application.result;


import com.evolutionnext.order.domain.aggregate.order.OrderId;

public record OrderItemAdded(OrderId orderId) implements OrderResult {
}
