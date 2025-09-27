package com.evolutionnext.customers.application.service;


import com.evolutionnext.customers.application.command.CustomerCommand;
import com.evolutionnext.customers.application.result.CustomerResult;
import com.evolutionnext.customers.domain.aggregate.Customer;
import com.evolutionnext.customers.port.in.PublicCustomerCommandPort;
import com.evolutionnext.customers.port.out.CustomerPublisher;

public class CustomerApplicationService implements PublicCustomerCommandPort {

    private final CustomerPublisher customerPublisher;

    public CustomerApplicationService(CustomerPublisher customerPublisher) {
        this.customerPublisher = customerPublisher;
    }

    @Override
    public CustomerResult submit(CustomerCommand customerCommand) {
        return switch (customerCommand) {
            case CustomerCommand.Create(String firstName, String lastName, String email, String state) -> {
                Customer customer = Customer.of(firstName, lastName, email, state);
                System.out.printf("Created customer: %s with events: %s", customer, customer.events());
                customer.events().stream().forEach(productEvent -> {
                    System.out.printf("Publishing event: %s", productEvent);
                    customerPublisher.publish(productEvent);
                });
                yield new CustomerResult.Created(customer.getId());
            }
        };
    }
}
