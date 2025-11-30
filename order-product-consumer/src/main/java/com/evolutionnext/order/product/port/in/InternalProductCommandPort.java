package com.evolutionnext.order.product.port.in;


import com.evolutionnext.order.product.application.command.InventoryCommand;
import com.evolutionnext.order.product.application.result.InventoryCommandResult;

public interface InternalProductCommandPort {
    InventoryCommandResult submit(InventoryCommand command);
}
