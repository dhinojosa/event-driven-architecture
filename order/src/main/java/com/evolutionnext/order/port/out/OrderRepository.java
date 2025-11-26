package com.evolutionnext.order.port.out;


import com.evolutionnext.order.domain.aggregate.order.Order;

public interface OrderRepository {
    Order save(Order order);
    Order findById(String id);
    void delete(Order order);
}
