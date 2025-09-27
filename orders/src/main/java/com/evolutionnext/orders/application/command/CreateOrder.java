package com.evolutionnext.orders.application.command;


import com.evolutionnext.orders.domain.aggregate.customer.CustomerId;
import com.evolutionnext.orders.domain.aggregate.order.OrderId;

public record CreateOrder(OrderId orderId, CustomerId customerId) implements OrderCommand {
}
