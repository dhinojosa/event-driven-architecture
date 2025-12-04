package com.evolutionnext.order.infrastructure.adapter.out;


import com.evolutionnext.order.domain.aggregate.product.Product;
import com.evolutionnext.order.domain.aggregate.product.ProductId;
import com.evolutionnext.order.port.out.ProductRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgresProductRepository implements ProductRepository {
    private final DataSource dataSource;

    public PostgresProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Product> findAllAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT productid, name, price FROM product where stock > 0";

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Product product = new Product(
                    new ProductId(UUID.fromString(resultSet.getString("productid"))),
                    resultSet.getString("name"),
                    resultSet.getBigDecimal("price")
                );
                products.add(product);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products from database", e);
        }
        return products;
    }
}
