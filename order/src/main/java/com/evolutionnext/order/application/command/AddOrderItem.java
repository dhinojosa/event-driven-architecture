package com.evolutionnext.order.application.command;

import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.product.ProductId;

public record AddOrderItem(OrderId orderId, ProductId productId, int quantity) implements OrderCommand {
}
