package com.evolutionnext.order.port.in;


import java.util.UUID;

public interface InternalReceivingProductPort {
    public void storeProduct(UUID id, String name);
}
