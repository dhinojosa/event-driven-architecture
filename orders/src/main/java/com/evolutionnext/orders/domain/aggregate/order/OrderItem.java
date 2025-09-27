package com.evolutionnext.orders.domain.aggregate.order;


import com.evolutionnext.orders.domain.aggregate.product.ProductId;

public record OrderItem(ProductId productId, int quantity, int price) {
}
