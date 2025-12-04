package com.evolutionnext.order.port.in;


import com.evolutionnext.order.domain.aggregate.product.Product;

import java.util.List;

public interface PublicProductQueryPort {
    public List<Product> findAllAvailableProducts();
}
