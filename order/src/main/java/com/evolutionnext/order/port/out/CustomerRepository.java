package com.evolutionnext.order.port.out;

import com.evolutionnext.order.domain.aggregate.customer.Customer;

import java.util.List;

public interface CustomerRepository {
    List<Customer> findAllCustomers();
}
