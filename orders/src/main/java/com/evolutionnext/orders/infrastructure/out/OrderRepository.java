package com.evolutionnext.orders.infrastructure.out;


import com.evolutionnext.orders.domain.aggregate.order.Order;

public interface OrderRepository {
    Order save(Order order);
    Order findById(String id);
    void delete(Order order);
}
