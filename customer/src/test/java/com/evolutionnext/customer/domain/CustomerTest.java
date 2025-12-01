package com.evolutionnext.customer.domain;


import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.events.CustomerEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerTest {
    @Test
    void testCustomerEvents() {
        Customer customer = Customer.of("John", "Doe", "jdoe@aol.com", "NY");
        List<CustomerEvent> events = customer.events();
        assertThat(events).hasSize(1);
    }
}
