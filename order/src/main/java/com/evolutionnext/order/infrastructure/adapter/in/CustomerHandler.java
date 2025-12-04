package com.evolutionnext.order.infrastructure.adapter.in;

import com.evolutionnext.order.port.in.PublicCustomerQueryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CustomerHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomerHandler.class);
    private final ObjectMapper objectMapper;
    private final PublicCustomerQueryPort publicCustomerQueryPort;

    public CustomerHandler(PublicCustomerQueryPort publicCustomerQueryPort) {
        this.publicCustomerQueryPort = publicCustomerQueryPort;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Received {} request from {}", exchange.getRequestMethod(), exchange.getRemoteAddress());
        if ("GET".equals(exchange.getRequestMethod())) {
            logger.info("Retrieving all customers");
            var customers = publicCustomerQueryPort.findAllCustomers();
            var jsonResponse = objectMapper.writeValueAsString(customers);
            logger.info("Found {} customers", customers.size());

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}
