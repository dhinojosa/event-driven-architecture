package com.evolutionnext.customers.application.result;


import com.evolutionnext.customers.domain.aggregate.CustomerId;

public interface CustomerResult {
    record Created(CustomerId customerId) implements CustomerResult{}
}
