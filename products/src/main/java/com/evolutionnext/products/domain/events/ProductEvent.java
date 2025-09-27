package com.evolutionnext.products.domain.events;


import com.evolutionnext.customers.domain.events.ProductCreated;

public sealed interface ProductEvent permits ProductCreated {
}
