package com.evolutionnext.products.domain.events;


import com.evolutionnext.customers.domain.aggregate.Product;

public record ProductCreated(Product product) implements ProductEvent {
}
