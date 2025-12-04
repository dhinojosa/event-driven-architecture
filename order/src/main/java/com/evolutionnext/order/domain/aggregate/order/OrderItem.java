package com.evolutionnext.order.domain.aggregate.order;


import com.evolutionnext.order.domain.aggregate.product.ProductId;

import java.math.BigDecimal;

public record OrderItem(ProductId productId, int quantity, BigDecimal price) {
}
