package com.evolutionnext.orders.customer.products.domain.aggregate;


public record Product(ProductId productId, String name, String description, int price) {
}
