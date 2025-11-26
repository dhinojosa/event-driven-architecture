package com.evolutionnext.order.application.command;


public sealed interface OrderCommand permits CreateOrder, PlaceOrder, AddOrderItem, CancelOrder {
}
