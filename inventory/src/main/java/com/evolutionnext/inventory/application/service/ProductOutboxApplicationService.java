package com.evolutionnext.inventory.application.service;


import com.evolutionnext.inventory.application.command.ProductCommand;
import com.evolutionnext.inventory.application.result.ProductResult;
import com.evolutionnext.inventory.domain.aggregate.Product;
import com.evolutionnext.inventory.domain.aggregate.ProductId;
import com.evolutionnext.inventory.domain.events.ProductEvent;
import com.evolutionnext.inventory.port.in.PublicProductCommandPort;
import com.evolutionnext.inventory.port.out.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductOutboxApplicationService implements PublicProductCommandPort {

    private static final Logger logger = LoggerFactory.getLogger(ProductOutboxApplicationService.class);
    private final ProductRepository productRepository;

    public ProductOutboxApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResult submit(ProductCommand productCommand) {
        return switch (productCommand) {
            case ProductCommand.Create(String name, String description, BigDecimal price, int stock) -> {
                ProductId productId = new ProductId(UUID.randomUUID());
                Product product = new Product(productId, name, description, price, stock);
                logger.info("Creating new product with ID: {}", productId);
                try {
                    productRepository.persist(product);
                    yield new ProductResult.Created(productId, name);
                } catch (Exception e) {
                    yield new ProductResult.Error(e.getMessage());
                }
            }
        };
    }
}
