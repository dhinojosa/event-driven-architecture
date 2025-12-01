package com.evolutionnext.order.customer.application.result;


import com.evolutionnext.order.customer.domain.aggregate.CustomerId;

public sealed interface CustomerCommandResult permits CustomerCommandResult.Created, CustomerCommandResult.Error {
    record Created(CustomerId customerId) implements CustomerCommandResult{}
    record Error(String message) implements CustomerCommandResult{}
}
