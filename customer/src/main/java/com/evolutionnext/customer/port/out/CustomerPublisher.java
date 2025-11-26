package com.evolutionnext.customer.port.out;


import com.evolutionnext.customer.domain.events.CustomerEvent;

public interface CustomerPublisher {
    void publish(CustomerEvent customerEvent);
}
