package com.evolutionnext.order;

import com.evolutionnext.order.application.service.InMemoryOrderCommandApplicationService;
import com.evolutionnext.order.application.service.OrderQueryApplicationService;
import com.evolutionnext.order.application.service.OutboxOrderCommandApplicationService;
import com.evolutionnext.order.events.OrderEventMessage;
import com.evolutionnext.order.infrastructure.adapter.in.CustomerHandler;
import com.evolutionnext.order.infrastructure.adapter.in.IndexHandler;
import com.evolutionnext.order.infrastructure.adapter.in.ProductHandler;
import com.evolutionnext.order.infrastructure.adapter.in.SimpleWebServer;
import com.evolutionnext.order.infrastructure.adapter.out.OrderEventKafkaPublisher;
import com.evolutionnext.order.infrastructure.adapter.out.PostgresCustomerRepository;
import com.evolutionnext.order.infrastructure.adapter.out.PostgresOrderRepository;
import com.evolutionnext.order.infrastructure.adapter.out.PostgresProductRepository;
import com.evolutionnext.order.port.in.PublicOrderCommandPort;
import com.evolutionnext.order.port.out.CustomerRepository;
import com.evolutionnext.order.port.out.OrderEventPublisher;
import com.evolutionnext.order.port.out.OrderRepository;
import com.evolutionnext.order.port.out.ProductRepository;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class OrderRunner {
    private static final Logger logger = LoggerFactory.getLogger(OrderRunner.class);

    public static void main(String[] args) throws IOException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5432});
        dataSource.setDatabaseName("orderdb");
        dataSource.setUser("postgres");
        dataSource.setPassword("postgres");

        CustomerRepository customerRepository = new PostgresCustomerRepository(dataSource);
        ProductRepository productRepository = new PostgresProductRepository(dataSource);

        OrderQueryApplicationService orderQueryApplicationService = new OrderQueryApplicationService(customerRepository, productRepository);

        SimpleWebServer simpleWebServer = new SimpleWebServer(
            new IndexHandler(createPublisherService()),
            new ProductHandler(orderQueryApplicationService),
            new CustomerHandler(orderQueryApplicationService));
        simpleWebServer.start(9003);
        logger.info("Server started on port 9003");
    }

    private static PublicOrderCommandPort createPublisherService() {
        OrderEventPublisher orderEventPublisher = new OrderEventKafkaPublisher("localhost:9092", "http://localhost:8081");
        return new InMemoryOrderCommandApplicationService(orderEventPublisher);
    }

    private static PublicOrderCommandPort createOutboxService(PGSimpleDataSource dataSource) {
        OrderRepository orderRepository = new PostgresOrderRepository(dataSource);
        return new OutboxOrderCommandApplicationService(orderRepository);
    }
}
