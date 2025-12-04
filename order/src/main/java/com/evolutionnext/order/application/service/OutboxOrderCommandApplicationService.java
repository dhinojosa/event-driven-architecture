package com.evolutionnext.order.application.service;


import com.evolutionnext.order.application.command.OrderCommand;
import com.evolutionnext.order.application.result.OrderResult;
import com.evolutionnext.order.application.result.OrderResult.OrderCancelled;
import com.evolutionnext.order.domain.aggregate.order.Order;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.domain.aggregate.order.OrderItem;
import com.evolutionnext.order.domain.aggregate.product.ProductId;
import com.evolutionnext.order.port.in.PublicOrderCommandPort;
import com.evolutionnext.order.port.out.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Optional;

public class OutboxOrderCommandApplicationService implements PublicOrderCommandPort {
    private static final Logger logger = LoggerFactory.getLogger(OutboxOrderCommandApplicationService.class);

    private final OrderRepository orderRepository;

    public OutboxOrderCommandApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderResult submit(OrderCommand orderCommand) {
        return switch(orderCommand) {
            case OrderCommand.AddOrderItem(var orderId, var productId, var quantity, var price) -> {
                Optional<Order> orderMaybe = orderRepository.findById(orderId);
                yield orderMaybe.<OrderResult>map(o -> {
                    o.addOrderItem(new OrderItem(productId, quantity, price));
                    orderRepository.update(o);
                    return new OrderResult.OrderItemAdded(orderId, productId);
                }).orElse(new OrderResult.NotFound(orderId));
            }
            case OrderCommand.CancelOrder(var orderId) -> {
                Optional<Order> orderMaybe = orderRepository.findById(orderId);
                yield orderMaybe.<OrderResult>map(o -> {
                    o.placeOrder();
                    orderRepository.update(o);
                    return new OrderCancelled(orderId);
                }).orElse(new OrderResult.NotFound(orderId));
            }
            case OrderCommand.CreateOrder(var orderId, var customerId) -> {
                Order order = new Order(orderId, customerId);
                orderRepository.persist(order);
                yield new OrderResult.OrderCreated(orderId);
            }
            case OrderCommand.PlaceOrder(var orderId) -> {
                Optional<Order> orderMaybe = orderRepository.findById(orderId);
                yield orderMaybe.<OrderResult>map(o -> {
                    o.placeOrder();
                    orderRepository.update(o);
                    return new OrderResult.OrderPlaced(orderId);
                }).orElse(new OrderResult.NotFound(orderId));
            }
        };
    };
}
