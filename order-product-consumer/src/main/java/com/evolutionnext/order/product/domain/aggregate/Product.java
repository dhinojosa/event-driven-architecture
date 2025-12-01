package com.evolutionnext.order.product.domain.aggregate;


import java.math.BigDecimal;

public record Product (ProductId productId, String name, String description, int stock, BigDecimal price){
}
