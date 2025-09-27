package com.evolutionnext.orders.port.in;


import com.evolutionnext.orders.application.command.OrderCommand;
import com.evolutionnext.orders.application.result.OrderResult;

public interface PublicOrderCommandPort {
    public OrderResult submit(OrderCommand orderCommand);
}
