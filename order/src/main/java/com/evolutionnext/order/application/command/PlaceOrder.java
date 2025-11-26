package com.evolutionnext.order.application.command;


import com.evolutionnext.order.domain.aggregate.order.OrderId;

public record PlaceOrder(OrderId orderId) implements OrderCommand {
}
