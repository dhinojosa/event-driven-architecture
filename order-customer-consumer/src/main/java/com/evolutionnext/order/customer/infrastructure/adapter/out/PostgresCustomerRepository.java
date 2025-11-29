package com.evolutionnext.order.customer.infrastructure.adapter.out;


import com.evolutionnext.order.customer.domain.aggregate.Customer;
import com.evolutionnext.order.customer.port.out.CustomerRepository;

import javax.sql.DataSource;

public class PostgresCustomerRepository implements CustomerRepository {
    private final DataSource dataSource;

    public PostgresCustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Customer customer) {


    }
}
