package com.evolutionnext.inventory.application.command;


import java.math.BigDecimal;

public sealed interface ProductCommand permits ProductCommand.Create {
    record Create(String name, String description, BigDecimal price) implements ProductCommand{}
}
