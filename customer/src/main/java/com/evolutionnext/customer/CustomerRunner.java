package com.evolutionnext.customer;


import com.evolutionnext.customer.application.service.CustomerOutboxApplicationService;
import com.evolutionnext.customer.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.customer.infrastructure.adapter.out.PostgresCustomerOutboxRepository;
import com.evolutionnext.customer.port.in.PublicCustomerCommandPort;
import com.evolutionnext.customer.port.out.CustomerRepository;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;

public class CustomerRunner {
    public static void main(String[] args) throws IOException {
//        CustomerPublisher customerPublisher = new KafkaCustomerPublisher("localhost:9092", "http://localhost:8081");
//        PublicCustomerCommandPort customerCommandPort = new CustomerPublisherApplicationService(customerPublisher);

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5434});
        dataSource.setDatabaseName("customerdb");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");

        CustomerRepository customerRepository = new PostgresCustomerOutboxRepository(dataSource);
        PublicCustomerCommandPort customerCommandPort = new CustomerOutboxApplicationService(customerRepository);

        SimpleWebServer simpleWebServer = new SimpleWebServer(customerCommandPort);
        simpleWebServer.start(9000);
        System.out.println("Server started on port 9000");
    }
}
