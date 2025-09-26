package com.evolutionnext.domain.application;


import com.evolutionnext.domain.aggregate.Order;
import com.evolutionnext.domain.aggregate.OrderEventPublisher;

public class OrderApplicationService {

    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public void submit(Order order) {
        order.events().forEach(orderEventPublisher::publish);
        order.clearEvents();
    }
}
