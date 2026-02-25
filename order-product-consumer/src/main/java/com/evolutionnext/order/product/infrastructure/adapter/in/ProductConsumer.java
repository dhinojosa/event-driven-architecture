package com.evolutionnext.order.product.infrastructure.adapter.in;

import com.evolutionnext.inventory.events.*;
import com.evolutionnext.order.product.application.command.InventoryCommand;
import com.evolutionnext.order.product.application.result.InventoryCommandResult;
import com.evolutionnext.order.product.domain.aggregate.ProductId;
import com.evolutionnext.order.product.port.in.InternalProductCommandPort;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ProductConsumer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ProductConsumer.class);
    private final InternalProductCommandPort port;
    private volatile boolean running = true;

    public ProductConsumer(InternalProductCommandPort port) {
        this.port = port;
    }

    public void run() {
        Properties props = getProperties();

        try (KafkaConsumer<String, InventoryEventMessage> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("inventory"));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down consumer...");
                running = false;
            }));

            while (running) {
                ConsumerRecords<String, InventoryEventMessage> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(record -> {
                    logger.info("Received message: key={}, value={}", record.key(), record.value());
                    InventoryEventMessage inventoryEventMessage = record.value();
                    Object event = inventoryEventMessage.getEvent();
                    InventoryCommand inventoryCommand = switch(event) {
                        case ProductCreatedMessage m -> new InventoryCommand.CreateProduct(
                            new ProductId(inventoryEventMessage.getProductId()), m.getName().toString(), m.getDescription().toString(), m.getStock(), BigDecimal.valueOf(m.getPrice()));
                        case PriceChangedMessage m -> new InventoryCommand.UpdatePrice(new ProductId(inventoryEventMessage.getProductId()),
                            BigDecimal.valueOf(m.getPrice()));
                        case StockChangedMessage m -> new InventoryCommand.UpdateStock(new ProductId(inventoryEventMessage.getProductId()),
                            m.getStock());
                        default -> throw new IllegalStateException("Unexpected value: " + event);
                    };
                    InventoryCommandResult result = port.submit(inventoryCommand);
                    logger.info("Inventory Command Result: {}", result);
                });
            }
        }
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "product-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put("specific.avro.reader", true);
        return props;
    }
}
