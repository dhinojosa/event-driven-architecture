package com.evolutionnext.order.domain.aggregate.customer;


public record Customer(CustomerId id, String firstName, String lastName, String email, String state) {
}
