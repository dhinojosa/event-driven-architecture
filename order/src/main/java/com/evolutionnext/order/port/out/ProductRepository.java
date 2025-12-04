package com.evolutionnext.order.port.out;

import com.evolutionnext.order.domain.aggregate.product.Product;

import java.util.List;

public interface ProductRepository {
    List<Product> findAllAvailableProducts();
}
