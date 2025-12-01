package com.evolutionnext.inventory.infrastructure.adapter.out;

import com.evolutionnext.inventory.domain.aggregate.Product;
import com.evolutionnext.inventory.domain.events.ProductEvent;
import com.evolutionnext.inventory.events.EventType;
import com.evolutionnext.inventory.events.InventoryEventMessage;
import com.evolutionnext.inventory.events.ProductCreatedMessage;
import com.evolutionnext.inventory.port.out.ProductPublisher;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Properties;

public class KafkaProductPublisher implements ProductPublisher {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProductPublisher.class);

    private final KafkaProducer<String, ProductCreatedMessage> producer;
    private static final String TOPIC = "products";

    public KafkaProductPublisher(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", schemaRegistryUrl);
        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void publish(ProductEvent productEvent) {
        ProducerRecord<String, ProductCreatedMessage> producerRecord =
            switch (productEvent) {
                case ProductEvent.ProductCreated(Product product) -> {
                    logger.info("Publishing product created event: {}", product);
                    ProductCreatedMessage message =
                        new ProductCreatedMessage(
                            product.name(),
                            product.description(),
                            product.price().doubleValue(),
                            product.stock()
                        );
                    InventoryEventMessage inventoryEventMessage =
                        new InventoryEventMessage(product.productId().id(),
                            Instant.now(), EventType.PRODUCT_CREATED, message);
                    yield new ProducerRecord<>
                        (TOPIC, product.productId().id().toString(), message);
                }
            };

        try {
            logger.info("Sending message: {}", producerRecord);
            producer.send(producerRecord);
            logger.info("Message sent successfully");
        } catch (Exception e) {
            logger.error("Error sending message", e);
            throw new RuntimeException(e);
        }
    }
}
