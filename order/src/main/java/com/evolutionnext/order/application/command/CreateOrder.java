package com.evolutionnext.order.application.command;


import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.domain.aggregate.order.OrderId;

public record CreateOrder(OrderId orderId, CustomerId customerId) implements OrderCommand {
}
