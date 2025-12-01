package com.evolutionnext.order.customer.application.service;


import com.evolutionnext.order.customer.application.command.CustomerCommand;
import com.evolutionnext.order.customer.application.result.CustomerCommandResult;
import com.evolutionnext.order.customer.domain.aggregate.Customer;
import com.evolutionnext.order.customer.domain.aggregate.CustomerId;
import com.evolutionnext.order.customer.port.in.MessagingCustomerCommandPort;
import com.evolutionnext.order.customer.port.out.CustomerRepository;

public class CustomerApplicationService implements MessagingCustomerCommandPort {

    private final CustomerRepository customerRepository;

    public CustomerApplicationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerCommandResult submit(CustomerCommand customerCommand) {
        return switch(customerCommand) {
            case CustomerCommand.Create(CustomerId customerId, String fullName) -> {
                try {
                    Customer customer = new Customer(customerId, fullName);
                    customerRepository.save(customer);
                    yield new CustomerCommandResult.Created(customerId);
                } catch (Exception e) {
                    yield new CustomerCommandResult.Error(e.getMessage());
                }
            }
        };
    }
}
