package com.evolutionnext.customer.application.result;


import com.evolutionnext.customer.domain.aggregate.CustomerId;

public interface CustomerResult {
    record Created(CustomerId customerId) implements CustomerResult{}
}
