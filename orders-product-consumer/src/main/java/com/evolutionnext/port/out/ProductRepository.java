package com.evolutionnext.port.out;


import com.evolutionnext.domain.aggregate.Product;

public interface ProductRepository {
    public void persist(Product product);
}
