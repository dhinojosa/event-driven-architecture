package com.evolutionnext.products.port.out;


import com.evolutionnext.products.domain.events.ProductEvent;

public interface ProductPublisher {
    void publish(ProductEvent productEvent);
}
