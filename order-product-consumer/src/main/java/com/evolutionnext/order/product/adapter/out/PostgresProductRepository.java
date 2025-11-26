package com.evolutionnext.order.product.adapter.out;


import com.evolutionnext.order.product.domain.aggregate.Product;
import com.evolutionnext.order.product.port.out.ProductRepository;

public class PostgresProductRepository implements ProductRepository {

    public PostgresProductRepository() {
    }

    @Override
    public void persist(Product product) {

    }
}
