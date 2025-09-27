package com.evolutionnext.orders.application.command;


public sealed interface OrderCommand permits CreateOrder, PlaceOrder, AddOrderItem, CancelOrder {
}
