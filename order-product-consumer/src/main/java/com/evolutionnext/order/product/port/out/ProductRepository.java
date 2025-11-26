package com.evolutionnext.order.product.port.out;


import com.evolutionnext.order.product.domain.aggregate.Product;

public interface ProductRepository {
    public void persist(Product product);
}
