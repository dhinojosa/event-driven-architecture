package com.evolutionnext.order.port.out;


import com.evolutionnext.order.domain.events.OrderEvent;

public interface OrderEventPublisher {
   public void publish(OrderEvent orderEvent);
}
