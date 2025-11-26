package com.evolutionnext.order.domain.events;


public sealed interface OrderEvent permits OrderCancelled, OrderCreated, OrderPlaced {

}
