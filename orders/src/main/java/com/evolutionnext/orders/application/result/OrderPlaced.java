package com.evolutionnext.orders.application.result;


import com.evolutionnext.orders.domain.aggregate.order.OrderId;

public record OrderPlaced(OrderId orderId) implements OrderResult{
}
