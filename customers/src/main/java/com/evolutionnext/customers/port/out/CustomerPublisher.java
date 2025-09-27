package com.evolutionnext.customers.port.out;


import com.evolutionnext.customers.domain.events.CustomerEvent;

public interface CustomerPublisher {
    void publish(CustomerEvent productEvent);
}
