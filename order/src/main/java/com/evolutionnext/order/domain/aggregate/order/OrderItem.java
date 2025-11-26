package com.evolutionnext.order.domain.aggregate.order;


import com.evolutionnext.order.domain.aggregate.product.ProductId;

public record OrderItem(ProductId productId, int quantity, int price) {
}
