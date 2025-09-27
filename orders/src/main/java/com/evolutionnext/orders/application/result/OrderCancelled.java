package com.evolutionnext.orders.application.result;


import com.evolutionnext.orders.domain.aggregate.order.OrderId;

public record OrderCancelled(OrderId orderId) implements OrderResult {
}
