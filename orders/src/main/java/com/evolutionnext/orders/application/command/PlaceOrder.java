package com.evolutionnext.orders.application.command;


import com.evolutionnext.orders.domain.aggregate.order.OrderId;

public record PlaceOrder(OrderId orderId) implements OrderCommand {
}
