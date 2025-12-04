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

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put("schema.registry.url", "http://localhost:8081");
        properties.put("auto.register.schemas", "false");
        properties.put("use.latest.version", "true");
        properties.put("latest.compatibility.strict", "false");

        KafkaProducer<String, OrderEventMessage> producer = new KafkaProducer<>(properties);
        SimpleWebServer simpleWebServer = createWebServer(dataSource, producer);
        simpleWebServer.start(9003);
        logger.info("Server started on port 9003");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Closing Kafka Producer");
            producer.close();
        }));
    }

    private static SimpleWebServer createWebServer(PGSimpleDataSource dataSource, KafkaProducer<String, OrderEventMessage> producer) {
        CustomerRepository customerRepository = new PostgresCustomerRepository(dataSource);
        ProductRepository productRepository = new PostgresProductRepository(dataSource);

        OrderQueryApplicationService orderQueryApplicationService = new OrderQueryApplicationService(customerRepository, productRepository);
//        OrderEventPublisher orderEventPublisher = new OrderEventKafkaPublisher(producer);
//        InMemoryOrderCommandApplicationService orderCommandApplicationService = new InMemoryOrderCommandApplicationService(orderEventPublisher);

        OrderRepository orderRepository = new PostgresOrderRepository(dataSource);
        OutboxOrderCommandApplicationService orderCommandApplicationService = new OutboxOrderCommandApplicationService(orderRepository);

        return new SimpleWebServer(
            new IndexHandler(orderCommandApplicationService),
            new ProductHandler(orderQueryApplicationService),
            new CustomerHandler(orderQueryApplicationService));
    }
}
