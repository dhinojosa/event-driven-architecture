package com.evolutionnext.order.application.command;


import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.product.ProductId;

import java.math.BigDecimal;

public sealed interface OrderCommand permits OrderCommand.AddOrderItem, OrderCommand.CancelOrder, OrderCommand.CreateOrder, OrderCommand.PlaceOrder {
    record CreateOrder(OrderId orderId, CustomerId customerId) implements OrderCommand {}
    record AddOrderItem(OrderId orderId, ProductId productId, int quantity, BigDecimal price) implements OrderCommand {}
    record PlaceOrder(OrderId orderId) implements OrderCommand {}
    record CancelOrder(OrderId orderId) implements OrderCommand {}
}
