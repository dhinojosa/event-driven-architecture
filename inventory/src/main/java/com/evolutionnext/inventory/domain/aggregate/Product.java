package com.evolutionnext.inventory.domain.aggregate;


public record Product(ProductId productId, String name, String description, int price) {
}
