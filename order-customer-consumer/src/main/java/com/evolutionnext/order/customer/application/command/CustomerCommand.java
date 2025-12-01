package com.evolutionnext.order.customer.application.command;


import com.evolutionnext.order.customer.domain.aggregate.CustomerId;

public sealed interface CustomerCommand permits CustomerCommand.Create {
    record Create(CustomerId customerId, String fullName) implements CustomerCommand{}
}
