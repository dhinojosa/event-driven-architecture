package com.evolutionnext.inventory;


import com.evolutionnext.inventory.application.service.ProductApplicationService;
import com.evolutionnext.inventory.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.inventory.infrastructure.adapter.out.KafkaProductPublisher;
import com.evolutionnext.inventory.port.out.ProductPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    public static void main(String[] args) throws IOException {
        ProductPublisher productPublisher =
            new KafkaProductPublisher("localhost:9092", "http://localhost:8081");
        SimpleWebServer simpleWebServer =
            new SimpleWebServer(new ProductApplicationService(productPublisher));
        simpleWebServer.start(9001);
        logger.info("Server started on port 9001");
    }
}
