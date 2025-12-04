package com.evolutionnext.customer.application.service;

import com.evolutionnext.customer.application.command.CustomerCommand;
import com.evolutionnext.customer.application.result.CustomerResult;
import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.aggregate.CustomerId;
import com.evolutionnext.customer.port.in.PublicCustomerCommandPort;
import com.evolutionnext.customer.port.out.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CustomerOutboxApplicationService implements PublicCustomerCommandPort {

    private static final Logger logger = LoggerFactory.getLogger(CustomerOutboxApplicationService.class);
    private final CustomerRepository customerRepository;

    public CustomerOutboxApplicationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResult submit(CustomerCommand customerCommand) {
        return switch (customerCommand) {
            case CustomerCommand.Create(String firstName, String lastName, String email, String state) -> {
                Customer customer = Customer.of(new CustomerId(UUID.randomUUID()), firstName, lastName, email, state);
                logger.info("Created customer: {} with events: {}", customer, customer.events());
                customer.events().forEach(customerEvent -> {
                    logger.info("Publishing event: {}", customerEvent);
                    customerRepository.persist(customer);
                });
                yield new CustomerResult.Created(customer.getId());
            }
        };
    }
}
