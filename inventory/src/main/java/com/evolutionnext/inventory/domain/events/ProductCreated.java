package com.evolutionnext.inventory.domain.events;

import com.evolutionnext.inventory.domain.aggregate.Product;

public record ProductCreated(Product product) implements ProductEvent {
}
