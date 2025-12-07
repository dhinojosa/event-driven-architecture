package com.evolutionnext.customer.infrastructure.adapter.out;


import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.aggregate.CustomerId;
import com.evolutionnext.customer.events.CustomerCreatedMessage;
import com.evolutionnext.customer.port.out.CustomerRepository;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.specific.SpecificRecordBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostgresCustomerRepository implements CustomerRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresCustomerRepository.class);
    private static final CustomerEvent CUSTOMER_CREATED = CustomerEvent.CUSTOMER_CREATED;

    private enum CustomerEvent {
        CUSTOMER_CREATED
    }

    private final DataSource dataSource;

    public PostgresCustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static byte[] toAvroBytes(SpecificRecordBase record) {
        try (KafkaAvroSerializer serializer = new KafkaAvroSerializer()) {
            serializer.configure(
                Map.of("schema.registry.url", "http://schema-registry:8081"),
                false
            );
            return serializer.serialize("customer", record);

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Avro to bytes", e);
        }
    }


    @Override
    public void persist(Customer customer) {


        logger.info("Persisting customer with ID: {}", customer.getId());
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String customerSql = "INSERT INTO public.customer (customerid, firstname, lastname, email, state) VALUES (?, ?, ?, ?, ?)";
                try (var customerStmt = connection.prepareStatement(customerSql)) {
                    customerStmt.setObject(1, customer.getId().id());
                    customerStmt.setString(2, customer.getFirstName());
                    customerStmt.setString(3, customer.getLastName());
                    customerStmt.setString(4, customer.getEmail());
                    customerStmt.setString(5, customer.getState());
                    customerStmt.executeUpdate();
                }

                CustomerCreatedMessage customerCreatedMessage = new CustomerCreatedMessage(
                    customer.getId().id().toString(),
                    Instant.now(),
                    customer.getFirstName(),
                    customer.getLastName(), customer.getEmail(), customer.getState()
                );

                String outboxSql = "INSERT INTO customer_outbox (id, aggregatetype, aggregateid, type, payload) VALUES (?, ?, ?, ?, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {
                    outboxStmt.setObject(1, java.util.UUID.randomUUID());
                    outboxStmt.setString(2, "Customer");
                    outboxStmt.setString(3, customer.getId().id().toString());
                    outboxStmt.setString(4, CUSTOMER_CREATED.toString());
                    outboxStmt.setBytes(5, toAvroBytes(customerCreatedMessage));
                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                e.printStackTrace();
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            logger.error("Failed to persist customer", e);
            throw new RuntimeException("Failed to persist customer", e);
        }
    }

    @Override
    public Customer findById(CustomerId customerId) {
        logger.info("Finding customer by ID: {}", customerId);
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                 "SELECT customerid, firstname, lastname, email, state FROM public.customer WHERE customerid = ?")) {
            preparedStatement.setObject(1, customerId.id());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Customer.of(
                    new CustomerId(resultSet.getObject("customerid", UUID.class)),
                    resultSet.getString("firstname"),
                    resultSet.getString("lastname"),
                    resultSet.getString("email"),
                    resultSet.getString("state")
                );
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to find customer by ID", e);
            throw new RuntimeException("Failed to find customer by ID", e);
        }
    }

    @Override
    public List<Customer> findAll() {
        logger.info("Finding all customers");
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                 "SELECT customerid, firstname, lastname, email, state FROM public.customer")) {
            var resultSet = preparedStatement.executeQuery();
            List<Customer> customers = new ArrayList<>();
            while (resultSet.next()) {
                customers.add(Customer.of(
                    new CustomerId(resultSet.getObject("customerid", UUID.class)),
                    resultSet.getString("firstname"),
                    resultSet.getString("lastname"),
                    resultSet.getString("email"),
                    resultSet.getString("state")
                ));
            }
            return customers;
        } catch (SQLException e) {
            logger.error("Failed to find all customers", e);
            throw new RuntimeException("Failed to find all customers", e);
        }
    }
}
