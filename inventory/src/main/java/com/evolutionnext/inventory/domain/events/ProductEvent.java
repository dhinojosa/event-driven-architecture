package com.evolutionnext.inventory.domain.events;

import com.evolutionnext.inventory.domain.aggregate.Product;

public sealed interface ProductEvent permits ProductEvent.ProductCreated {
    record ProductCreated(Product product) implements ProductEvent {
    }
}
