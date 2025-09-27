package com.evolutionnext.orders.application.command;


import com.evolutionnext.orders.domain.aggregate.order.OrderId;
import com.evolutionnext.orders.domain.aggregate.product.ProductId;

public record AddOrderItem(OrderId orderId, ProductId productId, int quantity) implements OrderCommand {
}
