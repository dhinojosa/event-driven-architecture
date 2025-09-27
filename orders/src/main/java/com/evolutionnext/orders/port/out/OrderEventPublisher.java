package com.evolutionnext.orders.port.out;


import com.evolutionnext.orders.domain.events.OrderEvent;

public interface OrderEventPublisher {
    void publish(OrderEvent orderEvent);
}
