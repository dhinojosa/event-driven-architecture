package com.evolutionnext.inventory;


import com.evolutionnext.inventory.application.service.ProductOutboxApplicationService;
import com.evolutionnext.inventory.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.inventory.infrastructure.adapter.out.PostgresOutboxRepository;
import com.evolutionnext.inventory.port.in.PublicProductCommandPort;
import com.evolutionnext.inventory.port.out.ProductRepository;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class InventoryRunner {
    private static final Logger logger = LoggerFactory.getLogger(InventoryRunner.class);

    public static void main(String[] args) throws IOException {

        //Comment this when ready to switch over to outbox
        //ProductPublisher productPublisher =
        //    new KafkaProductPublisher("localhost:9092", "http://localhost:8081");
        //PublicProductCommandPort publicProductCommandPort =
        //    new ProductPublisherApplicationService(productPublisher);

        //Uncomment this when ready to switch over to outbox
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5433});
        dataSource.setDatabaseName("inventorydb");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");
        ProductRepository productRepository =
            new PostgresOutboxRepository(dataSource);
        PublicProductCommandPort publicProductCommandPort =
            new ProductOutboxApplicationService(productRepository);

        // The rest can stay the same
        SimpleWebServer simpleWebServer =
            new SimpleWebServer(publicProductCommandPort);
        simpleWebServer.start(9001);
        logger.info("Server started on port 9001");
    }
}
