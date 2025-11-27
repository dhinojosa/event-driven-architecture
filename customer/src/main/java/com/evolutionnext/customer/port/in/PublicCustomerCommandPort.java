package com.evolutionnext.customer.port.in;


import com.evolutionnext.customer.application.command.CustomerCommand;
import com.evolutionnext.customer.application.result.CustomerResult;

public interface PublicCustomerCommandPort {
    CustomerResult submit(CustomerCommand customerCommand);
}
