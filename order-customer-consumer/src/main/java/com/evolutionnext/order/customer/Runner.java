package com.evolutionnext.order.customer;


import com.evolutionnext.order.customer.application.service.CustomerApplicationService;
import com.evolutionnext.order.customer.domain.aggregate.CustomerId;
import com.evolutionnext.order.customer.infrastructure.adapter.in.KafkaCustomerConsumer;
import com.evolutionnext.order.customer.infrastructure.adapter.out.PostgresCustomerRepository;
import com.evolutionnext.order.customer.port.out.CustomerRepository;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

public class Runner {
    private static DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setDatabaseName("orderdb");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        dataSource.setPortNumbers(new int[]{5432});
        return dataSource;
    }

    public static void main(String[] args) {
        DataSource dataSource = createDataSource();
        CustomerRepository customerRepository = new PostgresCustomerRepository(dataSource);
        CustomerApplicationService customerApplicationService = new CustomerApplicationService(customerRepository);
        KafkaCustomerConsumer kafkaCustomerConsumer = new KafkaCustomerConsumer(customerApplicationService);
        kafkaCustomerConsumer.run();
    }
}
