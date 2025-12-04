package com.evolutionnext.order.port.in;

import com.evolutionnext.order.domain.aggregate.customer.Customer;

import java.util.List;

public interface PublicCustomerQueryPort {
    public List<Customer> findAllCustomers();
}
