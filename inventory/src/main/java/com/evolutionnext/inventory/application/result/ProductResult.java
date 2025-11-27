package com.evolutionnext.inventory.application.result;


import com.evolutionnext.inventory.domain.aggregate.ProductId;

public sealed interface ProductResult permits ProductResult.Created {
    record Created(ProductId productId) implements ProductResult{}
}
