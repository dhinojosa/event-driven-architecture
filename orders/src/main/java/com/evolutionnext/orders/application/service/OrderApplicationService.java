package com.evolutionnext.orders.application.service;


import com.evolutionnext.orders.application.command.*;
import com.evolutionnext.orders.application.result.OrderCancelled;
import com.evolutionnext.orders.application.result.OrderCreated;
import com.evolutionnext.orders.application.result.OrderItemAdded;
import com.evolutionnext.orders.application.result.OrderResult;
import com.evolutionnext.orders.domain.aggregate.order.Order;
import com.evolutionnext.orders.port.in.PublicOrderCommandPort;
import com.evolutionnext.orders.port.out.OrderEventPublisher;

public class OrderApplicationService implements PublicOrderCommandPort {

    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public OrderResult submit(OrderCommand orderCommand) {
        return switch(orderCommand) {
            case CreateOrder createOrder -> {
                Order order = new Order(createOrder.orderId(), createOrder.customerId());
                order.events().forEach(orderEventPublisher::publish);
                yield new OrderCreated(createOrder.orderId());
            }
            case AddOrderItem addOrderItem -> new OrderItemAdded(addOrderItem.orderId());
            case CancelOrder cancelOrder -> new OrderCancelled(cancelOrder.orderId());
            case PlaceOrder placeOrder -> new OrderCancelled(placeOrder.orderId());
        };
    }
}
