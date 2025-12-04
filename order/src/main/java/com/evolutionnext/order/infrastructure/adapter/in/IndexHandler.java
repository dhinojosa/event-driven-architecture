package com.evolutionnext.order.infrastructure.adapter.in;


import com.evolutionnext.order.application.command.OrderCommand;
import com.evolutionnext.order.application.result.OrderResult;
import com.evolutionnext.order.application.service.InMemoryOrderCommandApplicationService;
import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.product.ProductId;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.evolutionnext.orders.customer.web.FormParser.getFieldData;
import static com.evolutionnext.orders.customer.web.ResourceLoader.serveFromResources;

public class IndexHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(IndexHandler.class);
    private final InMemoryOrderCommandApplicationService orderCommandApplicationService;

    public IndexHandler(InMemoryOrderCommandApplicationService orderCommandApplicationService) {
        this.orderCommandApplicationService = orderCommandApplicationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            serveFromResources(exchange, "index.html");
            exchange.close();
        } else if ("POST".equals(exchange.getRequestMethod())) {
            Map<String, List<String>> params = getFieldData(exchange);
            if (params == null) {
                exchange.sendResponseHeaders(500, 0);
                exchange.close();
                return;
            }
            List<String> customerIdField = params.get("customerId");
            if (customerIdField == null || customerIdField.isEmpty()) {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
                return;
            }
            String customerIdString = customerIdField.getFirst();
            CustomerId customerId = new CustomerId(UUID.fromString(customerIdString));
            OrderId orderId = new OrderId(UUID.randomUUID());

            orderCommandApplicationService.submit(
                new OrderCommand.CreateOrder(orderId, customerId)
            );

            logger.info("params: {}", params);
            if (params.containsKey("productId") && params.containsKey("quantity")) {
                logger.info("Adding order items with products {} and quantities {}", params.get("productId"), params.get("quantity"));
                List<String> productIds = params.get("productId");
                List<String> quantities = params.get("quantity");
                List<String> prices = params.get("price");

                for (int i = 0; i < productIds.size(); i++) {
                    logger.info("Entering loop for product ids");
                    ProductId productId = new ProductId(UUID.fromString(productIds.get(i)));
                    int quantity = Integer.parseInt(quantities.get(i));
                    BigDecimal price = new BigDecimal(prices.get(i));

                    orderCommandApplicationService.submit(
                        new OrderCommand.AddOrderItem(orderId, productId, quantity, price)
                    );
                }
            }

            OrderResult result = orderCommandApplicationService.submit(
                new OrderCommand.PlaceOrder(orderId)
            );

            switch(result) {
                case OrderResult.Error error -> {
                    exchange.sendResponseHeaders(500, error.string().length());
                    exchange.getResponseBody().write(error.string().getBytes());
                }
                case OrderResult.OrderPlaced orderPlaced -> {
                    exchange.getResponseHeaders().add("Location", "/order?message=Order%20Placed");
                    exchange.sendResponseHeaders(303, -1);
                }
                default -> {
                    String message = "Not implemented";
                    exchange.sendResponseHeaders(501, message.length());
                    exchange.getResponseBody().write(message.getBytes());
                }
            }

            exchange.close();
        }
    }
}
