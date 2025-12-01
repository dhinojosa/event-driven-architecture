package com.evolutionnext.inventory.port.out;


import com.evolutionnext.inventory.domain.aggregate.Product;
import com.evolutionnext.inventory.domain.aggregate.ProductId;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository {
    void persist(Product product);
    void updatePrice(ProductId productId, BigDecimal price);
    Product findById(ProductId productId);
    void updateStock(ProductId productId, int stock);
    List<Product> findAll();
}
