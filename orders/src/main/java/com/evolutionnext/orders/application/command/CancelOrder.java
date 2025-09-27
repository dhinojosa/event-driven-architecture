package com.evolutionnext.orders.application.command;

import com.evolutionnext.orders.domain.aggregate.order.OrderId;

public record CancelOrder(OrderId orderId) implements OrderCommand {
}
