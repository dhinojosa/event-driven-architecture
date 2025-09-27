package com.evolutionnext.orders.port.in;


import com.evolutionnext.orders.domain.aggregate.customer.Customer;
import com.evolutionnext.orders.domain.aggregate.product.Product;

import java.util.List;

public interface InternalProductQueryPort {
    public List<Product> findProductLikeName(String fuzzyName);
}
