package com.evolutionnext.order.customer.infrastructure.adapter.out;


import com.evolutionnext.order.customer.domain.aggregate.Customer;
import com.evolutionnext.order.customer.port.out.CustomerRepository;

import javax.sql.DataSource;
import java.sql.SQLException;

public class PostgresCustomerRepository implements CustomerRepository {
    private final DataSource dataSource;

    public PostgresCustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Customer customer) {
        var sql = "INSERT INTO customer (customerID, fullName) VALUES (?, ?)";
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, customer.id().id());
            preparedStatement.setString(2, customer.fullName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save customer", e);
        }
    }
}
