package com.evolutionnext.order.infrastructure.adapter.out;


import com.evolutionnext.order.domain.aggregate.customer.Customer;
import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.port.out.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgresCustomerRepository implements CustomerRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresCustomerRepository.class);
    private final DataSource dataSource;

    public PostgresCustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Customer> findAllCustomers() {
        logger.info("Fetching all customers from database");
        List<Customer> customers = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT customerid, fullname FROM customer");
             var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                var customerId = resultSet.getString("customerid");
                var fullName = resultSet.getString("fullname");
                customers.add(new Customer(
                    new CustomerId(UUID.fromString(customerId)),
                    fullName
                ));
                logger.debug("Found customer: {} with id: {}", fullName, customerId);
            }
            logger.info("Successfully retrieved {} customers", customers.size());
        } catch (SQLException e) {
            logger.error("Failed to fetch customers from database", e);
            throw new RuntimeException("Error fetching customers", e);
        }
        return customers;
    }
}
