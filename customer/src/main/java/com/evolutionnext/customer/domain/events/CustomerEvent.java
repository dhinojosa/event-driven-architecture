package com.evolutionnext.customer.domain.events;


import com.evolutionnext.customer.domain.aggregate.Customer;

public sealed interface CustomerEvent permits CustomerEvent.Created {
    record Created(Customer customer) implements CustomerEvent { }
}
