package com.evolutionnext.inventory.adapter.out;

import com.evolutionnext.inventory.domain.events.ProductEvent;
import com.evolutionnext.inventory.port.out.ProductPublisher;

public class KafkaProductPublisher implements ProductPublisher {

    @Override
    public void publish(ProductEvent productEvent) {

    }
}
