package com.evolutionnext.inventory.port.in;


import com.evolutionnext.inventory.application.command.ProductCommand;
import com.evolutionnext.inventory.application.result.ProductResult;

public interface PublicProductCommandPort {
    ProductResult submit(ProductCommand productCommand);
}
