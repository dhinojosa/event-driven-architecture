package com.evolutionnext.order.application.service;

import com.evolutionnext.order.application.command.OrderCommand;
import com.evolutionnext.order.application.result.OrderResult;
import com.evolutionnext.order.domain.aggregate.order.Order;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.order.OrderItem;
import com.evolutionnext.order.port.in.PublicOrderCommandPort;
import com.evolutionnext.order.port.out.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOrderCommandApplicationService implements PublicOrderCommandPort {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrderCommandApplicationService.class);
    private final OrderEventPublisher orderEventPublisher;
    private final Map<OrderId, Order> orderStore = new ConcurrentHashMap<>();

    public InMemoryOrderCommandApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public OrderResult submit(OrderCommand orderCommand) {
        return switch (orderCommand) {
            case OrderCommand.CreateOrder(var id, var customerId) -> {
                if (orderStore.containsKey(id)) {
                    yield new OrderResult.Error("Order already exists");
                }
                Order order = new Order(id, customerId);
                orderStore.put(id, order);
                logger.info("Created new order with id: {}", id);
                yield new OrderResult.OrderCreated(id);
            }

            case OrderCommand.AddOrderItem(var id, var productId, var qty, var price) -> {
                Order order = orderStore.get(id);
                if (order == null) {
                    yield new OrderResult.Error("Order not found");
                }
                try {
                    order.addOrderItem(new OrderItem(productId, qty, price));
                    logger.info("Added item to order {}: product={}, quantity={}", id, productId, qty);
                    yield new OrderResult.OrderItemAdded(id, productId);
                } catch (Exception e) {
                    logger.error("Error adding item to order", e);
                    yield new OrderResult.Error(e.getMessage());
                }
            }

            case OrderCommand.PlaceOrder(var id) -> {
                Order order = orderStore.get(id);
                if (order == null) {
                    yield new OrderResult.Error("Order not found");
                }
                try {
                    order.placeOrder();
                    order.events().forEach(orderEventPublisher::publish);
                    order.clearEvents();
                    logger.info("Order placed successfully: {}", id);
                    yield new OrderResult.OrderPlaced(id);
                } catch (Exception e) {
                    logger.error("Error placing order", e);
                    yield new OrderResult.Error(e.getMessage());
                }
            }

            case OrderCommand.CancelOrder(var id) -> {
                Order order = orderStore.get(id);
                if (order == null) {
                    yield new OrderResult.Error("Order not found");
                }
                try {
                    order.cancelOrder();
                    order.events().forEach(orderEventPublisher::publish);
                    order.clearEvents();
                    logger.info("Order cancelled: {}", id);
                    yield new OrderResult.OrderCancelled(id);
                } catch (Exception e) {
                    logger.error("Error cancelling order", e);
                    yield new OrderResult.Error(e.getMessage());
                }
            }
        };
    }
}
