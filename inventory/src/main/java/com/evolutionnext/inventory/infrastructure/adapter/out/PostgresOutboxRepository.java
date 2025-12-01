package com.evolutionnext.inventory.infrastructure.adapter.out;

import com.evolutionnext.inventory.domain.aggregate.Product;
import com.evolutionnext.inventory.domain.aggregate.ProductId;
import com.evolutionnext.inventory.port.out.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.postgresql.util.PGobject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresOutboxRepository implements ProductRepository {
    private final DataSource dataSource;

    public PostgresOutboxRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void persist(Product product) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String productSql = "INSERT INTO product (productid, name, description, price, stock) VALUES (?, ?, ?, ?, ?)";
                try (var productStmt = connection.prepareStatement(productSql)) {
                    productStmt.setObject(1, product.productId().id());
                    productStmt.setString(2, product.name());
                    productStmt.setString(3, product.description());
                    productStmt.setBigDecimal(4, product.price());
                    productStmt.setInt(5, product.stock());
                    productStmt.executeUpdate();
                }

                String outboxSql = "INSERT INTO product_outbox (aggregate_type, aggregate_id, event_type, payload) VALUES (?, ?, ?, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode payload = mapper.createObjectNode()
                        .put("id", product.productId().id().toString())
                        .put("name", product.name())
                        .put("description", product.description())
                        .put("price", product.price().toString())
                        .put("stock", product.stock());

                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("jsonb");
                    jsonObject.setValue(payload.toString());

                    outboxStmt.setString(1, "Product");
                    outboxStmt.setObject(2, product.productId().id());
                    outboxStmt.setString(3, "ProductCreated");
                    outboxStmt.setObject(4, jsonObject);
                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error with database connection", e);
        }
    }

    @Override
    public void updatePrice(ProductId productId, BigDecimal price) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String productSql = "UPDATE product SET price = ? WHERE productid = ?";
                try (var productStmt = connection.prepareStatement(productSql)) {
                    productStmt.setBigDecimal(1, price);
                    productStmt.setObject(2, productId.id());
                    productStmt.executeUpdate();
                }

                String outboxSql = "INSERT INTO product_outbox (aggregate_type, aggregate_id, event_type, payload) VALUES (?, ?, ?, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode payload = mapper.createObjectNode()
                        .put("id", productId.id().toString())
                        .put("price", price.toString());

                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("jsonb");
                    jsonObject.setValue(payload.toString());

                    outboxStmt.setString(1, "Product");
                    outboxStmt.setObject(2, productId.id());
                    outboxStmt.setString(3, "PriceUpdated");
                    outboxStmt.setObject(4, jsonObject);
                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Error updating price and outbox", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error with database connection", e);
        }
    }

    @Override
    public Product findById(ProductId productId) {
        String sql = "SELECT * FROM product WHERE productid = ?";
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, productId.id());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new Product(
                    new ProductId(resultSet.getObject("productid", java.util.UUID.class)),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getBigDecimal("price"),
                    resultSet.getInt("stock")
                );
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product", e);
        }
    }

    @Override
    public void updateStock(ProductId productId, int stock) {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String productSql = "UPDATE product SET stock = ? WHERE productid = ?";
                try (var productStmt = connection.prepareStatement(productSql)) {
                    productStmt.setInt(1, stock);
                    productStmt.setObject(2, productId.id());
                    productStmt.executeUpdate();
                }

                String outboxSql = "INSERT INTO product_outbox (aggregate_type, aggregate_id, event_type, payload) VALUES (?, ?, ?, ?)";
                try (var outboxStmt = connection.prepareStatement(outboxSql)) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode payload = mapper.createObjectNode()
                        .put("id", productId.id().toString())
                        .put("stock", stock);

                    PGobject jsonObject = new PGobject();
                    jsonObject.setType("jsonb");
                    jsonObject.setValue(payload.toString());

                    outboxStmt.setString(1, "Product");
                    outboxStmt.setObject(2, productId.id());
                    outboxStmt.setString(3, "StockUpdated");
                    outboxStmt.setObject(4, jsonObject);
                    outboxStmt.executeUpdate();
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Error updating stock and outbox", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error with database connection", e);
        }
    }

    @Override
    public List<Product> findAll() {
        String sql = "SELECT * FROM product";
        List<Product> products = new ArrayList<>();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                products.add(new Product(
                    new ProductId(resultSet.getObject("productid", java.util.UUID.class)),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getBigDecimal("price"),
                    resultSet.getInt("stock")
                ));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all products", e);
        }
    }
}
