package com.evolutionnext.order.port.in;


import com.evolutionnext.order.application.command.OrderCommand;
import com.evolutionnext.order.application.result.OrderResult;

public interface PublicOrderCommandPort {
    public OrderResult submit(OrderCommand orderCommand);
}
