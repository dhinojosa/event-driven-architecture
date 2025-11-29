package com.evolutionnext.order.customer.port.out;


import com.evolutionnext.order.customer.domain.aggregate.Customer;
import com.evolutionnext.order.customer.domain.aggregate.CustomerId;

public interface CustomerRepository {
    void save(Customer customer);
}
