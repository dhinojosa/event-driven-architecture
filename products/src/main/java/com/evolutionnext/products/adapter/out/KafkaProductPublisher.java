package com.evolutionnext.products.adapter.out;


import com.evolutionnext.customers.domain.events.ProductEvent;
import com.evolutionnext.customers.port.out.ProductPublisher;

public class KafkaProductPublisher implements ProductPublisher {

    @Override
    public void publish(ProductEvent productEvent) {

    }
}
