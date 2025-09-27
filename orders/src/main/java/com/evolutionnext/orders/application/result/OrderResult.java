package com.evolutionnext.orders.application.result;


public sealed interface OrderResult permits OrderCancelled, OrderCreated, OrderItemAdded, OrderPlaced {
}
