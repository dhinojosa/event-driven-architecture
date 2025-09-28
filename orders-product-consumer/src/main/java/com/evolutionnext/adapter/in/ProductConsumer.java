package com.evolutionnext.adapter.in;

import com.evolutionnext.port.in.InternalProductCommandPort;
import com.evolutionnext.products.events.ProductCreated;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

public class ProductConsumer {

    private final InternalProductCommandPort port;

    public ProductConsumer(InternalProductCommandPort port) {
        this.port = port;
    }

    public void run() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "product-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, ProductCreated> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("product_events"));
            while (true) {
                ConsumerRecords<String, ProductCreated> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(record -> {
                    System.out.printf("Received message: key=%s, value=%s%n",
                        record.key(), record.value());
                    UUID.fromString(record.key());
                    record.value().getName();
                    port.storeProduct(record.value().getId(), record.value().getName());
                });
            }
        }
    }
}
