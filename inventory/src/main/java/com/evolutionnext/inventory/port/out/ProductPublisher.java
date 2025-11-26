package com.evolutionnext.inventory.port.out;


import com.evolutionnext.inventory.domain.events.ProductEvent;

public interface ProductPublisher {
    void publish(ProductEvent productEvent);
}
