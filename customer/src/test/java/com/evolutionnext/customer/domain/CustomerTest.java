package com.evolutionnext.customer.domain;


import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.aggregate.CustomerId;
import com.evolutionnext.customer.domain.events.CustomerEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerTest {
    @Test
    void testCustomerEvents() {
        Customer customer = Customer.of(new CustomerId(UUID.randomUUID()), "John", "Doe", "jdoe@aol.com", "NY");
        List<CustomerEvent> events = customer.events();
        assertThat(events).hasSize(1);
    }
}
