package com.evolutionnext.order.application.command;

import com.evolutionnext.order.domain.aggregate.order.OrderId;

public record CancelOrder(OrderId orderId) implements OrderCommand {
}
