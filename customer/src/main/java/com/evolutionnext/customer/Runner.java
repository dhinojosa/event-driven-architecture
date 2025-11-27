package com.evolutionnext.customer;


import com.evolutionnext.customer.application.service.CustomerApplicationService;
import com.evolutionnext.customer.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.customer.infrastructure.adapter.out.KafkaCustomerPublisher;
import com.evolutionnext.customer.port.out.CustomerPublisher;

import java.io.IOException;

public class Runner {
    public static void main(String[] args) throws IOException {
        CustomerPublisher customerPublisher = new KafkaCustomerPublisher("localhost:9092", "http://localhost:8081");
        SimpleWebServer simpleWebServer = new SimpleWebServer(new CustomerApplicationService(customerPublisher));
        simpleWebServer.start(9000);
        System.out.println("Server started on port 9000");
    }
}
