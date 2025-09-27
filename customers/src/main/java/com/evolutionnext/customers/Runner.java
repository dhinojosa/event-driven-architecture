package com.evolutionnext.customers;


import com.evolutionnext.customers.application.service.CustomerApplicationService;
import com.evolutionnext.customers.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.customers.infrastructure.adapter.out.KafkaCustomerPublisher;
import com.evolutionnext.customers.port.out.CustomerPublisher;

import java.io.IOException;

public class Runner {
    public static void main(String[] args) throws IOException {
        CustomerPublisher customerPublisher = new KafkaCustomerPublisher("localhost:9092", "http://localhost:8081");
        SimpleWebServer simpleWebServer = new SimpleWebServer(new CustomerApplicationService(customerPublisher));
        simpleWebServer.start();
        System.out.println("Server started on port 8080");
    }
}
