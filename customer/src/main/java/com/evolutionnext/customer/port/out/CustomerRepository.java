package com.evolutionnext.customer.port.out;


import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.aggregate.CustomerId;

import java.util.List;

public interface CustomerRepository {
    void persist(Customer product);
    Customer findById(CustomerId productId);
    List<Customer> findAll();
}
