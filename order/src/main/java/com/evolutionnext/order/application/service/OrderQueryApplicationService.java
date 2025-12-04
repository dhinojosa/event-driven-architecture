package com.evolutionnext.order.application.service;


import com.evolutionnext.order.domain.aggregate.customer.Customer;
import com.evolutionnext.order.domain.aggregate.product.Product;
import com.evolutionnext.order.port.in.PublicCustomerQueryPort;
import com.evolutionnext.order.port.in.PublicProductQueryPort;
import com.evolutionnext.order.port.out.CustomerRepository;
import com.evolutionnext.order.port.out.ProductRepository;

import java.util.List;

public class OrderQueryApplicationService implements PublicCustomerQueryPort, PublicProductQueryPort {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public OrderQueryApplicationService(CustomerRepository customerRepository,
                                        ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Customer> findAllCustomers() {
        return customerRepository.findAllCustomers();
    }

    @Override
    public List<Product> findAllAvailableProducts() {
        return productRepository.findAllAvailableProducts();
    }
}
