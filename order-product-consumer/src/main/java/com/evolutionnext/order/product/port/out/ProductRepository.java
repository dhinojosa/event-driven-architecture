package com.evolutionnext.order.product.port.out;


import com.evolutionnext.order.product.domain.aggregate.Product;
import com.evolutionnext.order.product.domain.aggregate.ProductId;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository {
    void persist(Product product);
    void updatePrice(ProductId productId, BigDecimal price);
    void updateStock(ProductId productId, int stock);
    Optional<Product> load(ProductId productId);
}
