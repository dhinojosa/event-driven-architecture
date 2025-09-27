package com.evolutionnext.customers.domain.events;


import com.evolutionnext.customers.domain.aggregate.Customer;

public sealed interface CustomerEvent permits CustomerEvent.Created {
    record Created(Customer customer) implements CustomerEvent { }
}
