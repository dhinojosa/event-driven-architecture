package com.evolutionnext.order.port.out;


import com.evolutionnext.order.domain.aggregate.order.Order;
import com.evolutionnext.order.domain.aggregate.order.OrderId;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void persist(Order order);
    Optional<Order> findById(OrderId orderId);
    List<Order> findAll();
    void update(Order o);
}
