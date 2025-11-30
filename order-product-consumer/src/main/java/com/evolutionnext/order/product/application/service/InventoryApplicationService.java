package com.evolutionnext.order.product.application.service;


import com.evolutionnext.order.product.application.command.InventoryCommand;
import com.evolutionnext.order.product.application.result.InventoryCommandResult;
import com.evolutionnext.order.product.domain.aggregate.Product;
import com.evolutionnext.order.product.port.in.InternalProductCommandPort;
import com.evolutionnext.order.product.port.out.ProductRepository;

import java.util.Optional;

public class InventoryApplicationService implements InternalProductCommandPort {
    private final ProductRepository productRepository;

    public InventoryApplicationService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public InventoryCommandResult submit(InventoryCommand command) {
        return switch (command) {
            case InventoryCommand.CreateProduct createProduct -> {
                Product product = new Product(
                    createProduct.productId(),
                    createProduct.name(),
                    createProduct.description(),
                    createProduct.stock(),
                    createProduct.price()
                );

                try {
                    productRepository.persist(product);
                    yield new InventoryCommandResult.ProductCreated(product.productId().id());
                } catch (Exception e) {
                    yield new InventoryCommandResult.Error(e.getMessage());
                }
            }
            case InventoryCommand.UpdatePrice updatePrice -> {
                Optional<Product> load = productRepository.load(updatePrice.productId());
                yield load.map(product -> {
                    try {
                        productRepository.updatePrice(updatePrice.productId(), updatePrice.price());
                        return new InventoryCommandResult.PriceUpdated(product.productId().id(), product.price(), updatePrice.price());
                    } catch (Exception e) {
                        return new InventoryCommandResult.Error(e.getMessage());
                    }
                }).orElse(new InventoryCommandResult.NotFound("Product Not Found"));
            }
            case InventoryCommand.UpdateStock updateStock -> productRepository.load(updateStock.productId())
                .map(product -> {
                    try {
                        productRepository.updateStock(updateStock.productId(), updateStock.stock());
                        return new InventoryCommandResult.StockUpdated(product.productId().id(), product.stock(), updateStock.stock());
                    } catch (Exception e) {
                        return new InventoryCommandResult.Error(e.getMessage());
                    }
                })
                .orElse(new InventoryCommandResult.NotFound("Product Not Found"));
        };
    }
}
