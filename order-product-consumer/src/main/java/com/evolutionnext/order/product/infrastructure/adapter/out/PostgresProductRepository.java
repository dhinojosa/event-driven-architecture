package com.evolutionnext.order.product.infrastructure.adapter.out;


import com.evolutionnext.order.product.domain.aggregate.Product;
import com.evolutionnext.order.product.domain.aggregate.ProductId;
import com.evolutionnext.order.product.port.out.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;

public class PostgresProductRepository implements ProductRepository {

    private static final Logger logger = LoggerFactory.getLogger(PostgresProductRepository.class);
    private final DataSource dataSource;

    public PostgresProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void persist(Product product) {
        logger.info("Persisting product with ID: {}", product.productId());
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                 "INSERT INTO public.product (productid, name, description, stock, price) VALUES (?, ?, ?, ?, ?)")) {
            preparedStatement.setObject(1, product.productId().id());
            preparedStatement.setString(2, product.name());
            preparedStatement.setString(3, product.description());
            preparedStatement.setInt(4, product.stock());
            preparedStatement.setBigDecimal(5, product.price());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to persist product", e);
            throw new RuntimeException("Failed to persist product", e);
        }
    }

    @Override
    public void updatePrice(ProductId productId, BigDecimal price) {
        logger.info("Updating price to {} for product ID: {}", price, productId);
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                 "UPDATE public.product SET price = ? WHERE productid = ?")) {
            preparedStatement.setBigDecimal(1, price);
            preparedStatement.setObject(2, productId.id());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update product price", e);
            throw new RuntimeException("Failed to update product price", e);
        }
    }

    @Override
    public void updateStock(ProductId productId, int stock) {
        logger.info("Updating stock to {} for product ID: {}", stock, productId);
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                 "UPDATE public.product SET stock = ? WHERE productid = ?")) {
            preparedStatement.setInt(1, stock);
            preparedStatement.setObject(2, productId.id());
            preparedStatement.executeUpdate();
            logger.info("Successfully persisted product with ID: {}", productId);
            logger.info("Successfully updated stock for product ID: {}", productId);
        } catch (SQLException e) {
            logger.error("Failed to update product stock", e);
            throw new RuntimeException("Failed to update product stock", e);
        }
    }

    @Override
    public Optional<Product> load(ProductId productId) {
        logger.info("Loading product with ID: {}", productId);
        try (var connection = dataSource.getConnection();
             var preparedStatement = connection.prepareStatement(
                 "SELECT productid, name, description, stock, price FROM public.product WHERE productid = ?")) {
            preparedStatement.setObject(1, productId.id());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var product = new Product(
                    new ProductId(resultSet.getObject("productid", UUID.class)),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getInt("stock"),
                    resultSet.getBigDecimal("price")
                );
                logger.info("Successfully loaded product with ID: {}", productId);
                return Optional.of(product);
            }
            logger.info("Product not found with ID: {}", productId);
            return Optional.empty();
        } catch (SQLException e) {
            logger.error("Failed to load product", e);
            throw new RuntimeException("Failed to load product", e);
        }
    }
}
