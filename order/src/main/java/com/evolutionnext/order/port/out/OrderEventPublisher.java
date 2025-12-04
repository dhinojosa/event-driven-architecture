package com.evolutionnext.order.port.out;


import com.evolutionnext.order.domain.events.OrderEvent;

public interface OrderEventPublisher {
   void publish(OrderEvent orderEvent);
}
