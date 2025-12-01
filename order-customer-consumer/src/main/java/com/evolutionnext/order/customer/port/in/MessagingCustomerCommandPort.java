package com.evolutionnext.order.customer.port.in;

import com.evolutionnext.order.customer.application.command.CustomerCommand;
import com.evolutionnext.order.customer.application.result.CustomerCommandResult;

public interface MessagingCustomerCommandPort {
    CustomerCommandResult submit(CustomerCommand customerCommand);
}
