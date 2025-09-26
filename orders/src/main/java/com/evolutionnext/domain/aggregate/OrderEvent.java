package com.evolutionnext.domain.aggregate;


public sealed interface OrderEvent permits OrderCancelled, OrderCreated, OrderPlaced {

}
