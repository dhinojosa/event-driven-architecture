package com.evolutionnext.products.adapter.out;


import com.evolutionnext.products.domain.events.ProductEvent;
import com.evolutionnext.products.port.out.ProductPublisher;

public class KafkaProductPublisher implements ProductPublisher {

    @Override
    public void publish(ProductEvent productEvent) {

    }
}
