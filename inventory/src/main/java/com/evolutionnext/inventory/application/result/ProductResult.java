package com.evolutionnext.inventory.application.result;


import com.evolutionnext.inventory.domain.aggregate.ProductId;

public sealed interface ProductResult permits ProductResult.Created, ProductResult.Error {
    record Created(ProductId productId, String name) implements ProductResult { }
    record Error(String message) implements ProductResult { }
}
