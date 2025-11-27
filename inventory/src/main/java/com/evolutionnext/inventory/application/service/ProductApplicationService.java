package com.evolutionnext.inventory.application.service;


import com.evolutionnext.inventory.application.command.ProductCommand;
import com.evolutionnext.inventory.application.result.ProductResult;
import com.evolutionnext.inventory.domain.aggregate.Product;
import com.evolutionnext.inventory.domain.aggregate.ProductId;
import com.evolutionnext.inventory.domain.events.ProductEvent;
import com.evolutionnext.inventory.port.in.PublicProductCommandPort;
import com.evolutionnext.inventory.port.out.ProductPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductApplicationService implements PublicProductCommandPort {
    private static final Logger logger = LoggerFactory.getLogger(ProductApplicationService.class);
    private final ProductPublisher productPublisher;

    public ProductApplicationService(ProductPublisher productPublisher) {
        this.productPublisher = productPublisher;
    }

    @Override
    public ProductResult submit(ProductCommand productCommand) {
        return switch (productCommand) {
            case ProductCommand.Create(String name, String description, BigDecimal price, int stock) -> {
                ProductId productId = new ProductId(UUID.randomUUID());
                Product product = new Product(productId, name, description, price, stock);
                logger.info("Creating new product with ID: {}", productId);
                productPublisher.publish(new ProductEvent.ProductCreated(product));
                yield new ProductResult.Created(productId, name);
            }
        };
    }
}
