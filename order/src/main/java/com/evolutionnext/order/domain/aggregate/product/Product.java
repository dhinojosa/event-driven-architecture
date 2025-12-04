package com.evolutionnext.order.domain.aggregate.product;


import java.math.BigDecimal;

public record Product(ProductId productId, String name, BigDecimal price) {
}
