package com.evolutionnext.inventory.domain.aggregate;


import java.math.BigDecimal;

public record Product(ProductId productId, String name, String description, BigDecimal price) {
}
