package com.evolutionnext.inventory;


import com.evolutionnext.inventory.application.service.ProductOutboxApplicationService;
import com.evolutionnext.inventory.application.service.ProductPublisherApplicationService;
import com.evolutionnext.inventory.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.inventory.infrastructure.adapter.out.KafkaProductPublisher;
import com.evolutionnext.inventory.infrastructure.adapter.out.PostgresProductRepository;
import com.evolutionnext.inventory.port.in.PublicProductCommandPort;
import com.evolutionnext.inventory.port.out.ProductPublisher;
import com.evolutionnext.inventory.port.out.ProductRepository;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InventoryRunner {
    private static final Logger logger = LoggerFactory.getLogger(InventoryRunner.class);

    public static void main(String[] args) throws IOException {
        SimpleWebServer simpleWebServer =
            new SimpleWebServer(createPublisherService());
        simpleWebServer.start(9001);
        logger.info("Server started on port 9001");
    }

    private static PublicProductCommandPort createOutboxService() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5433});
        dataSource.setDatabaseName("inventorydb");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        ProductRepository productRepository =
            new PostgresProductRepository(dataSource);
        return new ProductOutboxApplicationService(productRepository);
    }

    private static PublicProductCommandPort createPublisherService() {
        ProductPublisher productPublisher =
            new KafkaProductPublisher("localhost:9092", "http://localhost:8081");
        return new ProductPublisherApplicationService(productPublisher);
    }
}
