package com.evolutionnext.order.port.in;


import java.util.UUID;

public interface InternalReceivingCustomerPort {
    public void storeCustomer(UUID id, String firstName, String lastName, String email);
}
