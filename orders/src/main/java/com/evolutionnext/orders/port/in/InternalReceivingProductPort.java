package com.evolutionnext.orders.port.in;


import java.util.UUID;

public interface InternalReceivingProductPort {
    public void storeProduct(UUID id, String name);
}
