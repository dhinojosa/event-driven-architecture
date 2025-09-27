package com.evolutionnext.customers.port.in;


import com.evolutionnext.customers.application.command.CustomerCommand;
import com.evolutionnext.customers.application.result.CustomerResult;

public interface PublicCustomerCommandPort {
    public CustomerResult submit(CustomerCommand customerCommand);
}
