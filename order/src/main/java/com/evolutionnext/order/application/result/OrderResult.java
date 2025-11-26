package com.evolutionnext.order.application.result;


public sealed interface OrderResult permits OrderCancelled, OrderCreated, OrderItemAdded, OrderPlaced {
}
