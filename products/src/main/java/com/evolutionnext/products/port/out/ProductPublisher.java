package com.evolutionnext.products.port.out;


import com.evolutionnext.customers.domain.events.ProductEvent;

public interface ProductPublisher {
    void publish(ProductEvent productEvent);
}
