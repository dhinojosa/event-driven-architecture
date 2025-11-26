package com.evolutionnext.customer.application.service;

import com.evolutionnext.customer.application.command.CustomerCommand;
import com.evolutionnext.customer.application.result.CustomerResult;
import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.port.in.PublicCustomerCommandPort;
import com.evolutionnext.customer.port.out.CustomerPublisher;

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
                customer.events().stream().forEach(customerEvent -> {
                    System.out.printf("Publishing event: %s", customerEvent);
                    customerPublisher.publish(customerEvent);
                });
                yield new CustomerResult.Created(customer.getId());
            }
        };
    }
}
