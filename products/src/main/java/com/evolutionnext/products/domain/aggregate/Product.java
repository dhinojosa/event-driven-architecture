package com.evolutionnext.products.domain.aggregate;


import com.evolutionnext.products.domain.aggregate.ProductId;

public record Product(ProductId productId, String name, String description, int price) {
}
