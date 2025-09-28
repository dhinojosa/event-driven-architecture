package com.evolutionnext.adapter.out;


import com.evolutionnext.domain.aggregate.Product;
import com.evolutionnext.port.out.ProductRepository;

public class PostgresProductRepository implements ProductRepository {

    public PostgresProductRepository() {
    }

    @Override
    public void persist(Product product) {

    }
}
