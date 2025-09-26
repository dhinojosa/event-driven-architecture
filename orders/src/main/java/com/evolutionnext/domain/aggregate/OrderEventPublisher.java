package com.evolutionnext.domain.aggregate;


public interface OrderEventPublisher {
    void publish(OrderEvent orderEvent);
}
