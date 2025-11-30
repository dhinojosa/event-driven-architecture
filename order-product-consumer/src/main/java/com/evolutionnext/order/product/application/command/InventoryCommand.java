package com.evolutionnext.order.product.application.command;


import com.evolutionnext.order.product.domain.aggregate.ProductId;

import java.math.BigDecimal;

public sealed interface InventoryCommand permits InventoryCommand.CreateProduct, InventoryCommand.UpdateStock, InventoryCommand.UpdatePrice {
    record CreateProduct(ProductId productId, String name, String description, int stock, BigDecimal price) implements InventoryCommand {}
    record UpdateStock(ProductId productId, int stock) implements InventoryCommand {}
    record UpdatePrice(ProductId productId, BigDecimal price) implements InventoryCommand {}
}
