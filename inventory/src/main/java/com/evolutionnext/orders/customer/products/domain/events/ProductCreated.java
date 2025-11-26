package com.evolutionnext.orders.customer.products.domain.events;

import com.evolutionnext.products.domain.aggregate.Product;

public record ProductCreated(Product product) implements ProductEvent {
}
