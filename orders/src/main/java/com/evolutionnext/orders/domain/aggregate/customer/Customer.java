package com.evolutionnext.orders.domain.aggregate.customer;


public record Customer(CustomerId id, String firstName, String lastName, String email, String state) {
}
