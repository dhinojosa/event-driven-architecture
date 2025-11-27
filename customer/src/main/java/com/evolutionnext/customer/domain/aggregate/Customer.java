package com.evolutionnext.customer.domain.aggregate;



import com.evolutionnext.customer.domain.events.CustomerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Customer {
    private final CustomerId id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String state;
    public final List<CustomerEvent> events;

    private Customer(CustomerId id, String firstName, String lastName, String email, String state, List<CustomerEvent> events) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.state = state;
        this.events = events;
    }

    public static Customer of(String firstName, String lastName, String email, String state) {
        CustomerId id = new CustomerId(UUID.randomUUID());
        ArrayList<CustomerEvent> events = new ArrayList<>();
        Customer customer = new Customer(id, firstName, lastName, email, state, events);
        customer.events.add(new CustomerEvent.Created(customer));
        return customer;
    }

    public CustomerId getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getState() {
        return state;
    }

    public List<CustomerEvent> events() {
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) &&
               Objects.equals(firstName, customer.firstName) &&
               Objects.equals(lastName, customer.lastName) &&
               Objects.equals(email, customer.email) &&
               Objects.equals(state, customer.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, state);
    }

    @Override
    public String toString() {
        return "Customer{" +
               "id=" + id +
               ", firstName='" + firstName + '\'' +
               ", lastName='" + lastName + '\'' +
               ", email='" + email + '\'' +
               ", state='" + state + '\'' +
               '}';
    }
}
