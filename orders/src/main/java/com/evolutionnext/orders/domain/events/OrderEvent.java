package com.evolutionnext.orders.domain.events;


public sealed interface OrderEvent permits OrderCancelled, OrderCreated, OrderPlaced {

}
