package com.evolutionnext.order.product.application.result;


import java.math.BigDecimal;
import java.util.UUID;

public sealed interface InventoryCommandResult permits InventoryCommandResult.Error, InventoryCommandResult.NotFound, InventoryCommandResult.PriceUpdated, InventoryCommandResult.ProductCreated, InventoryCommandResult.StockUpdated {
    record ProductCreated(UUID uuid) implements InventoryCommandResult{}
    record PriceUpdated(UUID uuid, BigDecimal previousPrice, BigDecimal updatedPrice) implements InventoryCommandResult {}
    record StockUpdated(UUID uuid, int previousStock, int updatedStock) implements InventoryCommandResult {}
    record Error(String message) implements InventoryCommandResult{}
    record NotFound(String message) implements InventoryCommandResult{}
}
