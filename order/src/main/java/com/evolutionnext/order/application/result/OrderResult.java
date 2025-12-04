package com.evolutionnext.order.application.result;


import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.product.ProductId;

public sealed interface OrderResult permits OrderResult.Error, OrderResult.NotFound, OrderResult.OrderCancelled, OrderResult.OrderCreated, OrderResult.OrderItemAdded, OrderResult.OrderPlaced {

    record OrderCancelled(OrderId orderId) implements OrderResult {
    }

    record OrderPlaced(OrderId orderId) implements OrderResult {
    }

    record Error(String string) implements OrderResult {
    }

    public record OrderItemAdded(OrderId id, ProductId productId) implements OrderResult {
    }

    public record OrderCreated(OrderId id) implements OrderResult {
    }

    public record NotFound(OrderId orderId) implements OrderResult {
    }
}
