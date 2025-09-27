package com.evolutionnext.orders.port.in;


import com.evolutionnext.orders.domain.aggregate.customer.Customer;

import java.util.List;

public interface InternalCustomerQueryPort {
    public List<Customer> findCustomerLikeName(String fuzzyName);
}
