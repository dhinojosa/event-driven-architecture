package com.evolutionnext.order.infrastructure.adapter.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.evolutionnext.order.port.in.PublicProductQueryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ProductHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProductHandler.class);
    private final PublicProductQueryPort publicProductQueryPort;
    private final ObjectMapper objectMapper;

    public ProductHandler(PublicProductQueryPort publicProductQueryPort) {
        this.publicProductQueryPort = publicProductQueryPort;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            logger.info("Handling GET request for products");
            var products = publicProductQueryPort.findAllAvailableProducts();
            var jsonResponse = objectMapper.writeValueAsString(products);
            logger.info("Found {} products", products.size());

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
            }
        } else {
            logger.warn("Method {} not allowed", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
        }
    }
}
