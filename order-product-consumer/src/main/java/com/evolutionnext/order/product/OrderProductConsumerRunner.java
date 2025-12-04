package com.evolutionnext.order.product;


import com.evolutionnext.order.product.adapter.in.ProductConsumer;
import com.evolutionnext.order.product.adapter.out.PostgresProductRepository;
import com.evolutionnext.order.product.application.service.InventoryApplicationService;
import com.evolutionnext.order.product.port.in.InternalProductCommandPort;
import com.evolutionnext.order.product.port.out.ProductRepository;
import org.postgresql.ds.PGSimpleDataSource;

public class OrderProductConsumerRunner {
    private static PGSimpleDataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5432});
        dataSource.setDatabaseName("orderdb");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        return dataSource;
    }

    public static void main(String[] args) {
        ProductRepository productRepository = new PostgresProductRepository(createDataSource());
        InternalProductCommandPort port = new InventoryApplicationService(productRepository);
        ProductConsumer productConsumer = new ProductConsumer(port);
        productConsumer.run();
    }
}
