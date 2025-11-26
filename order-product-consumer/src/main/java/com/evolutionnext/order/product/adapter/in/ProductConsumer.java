package com.evolutionnext.order.product.adapter.in;

import com.evolutionnext.order.product.port.in.InternalProductCommandPort;
import com.evolutionnext.product.events.ProductCreated;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ProductConsumer {

    private final InternalProductCommandPort port;

    public ProductConsumer(InternalProductCommandPort port) {
        this.port = port;
    }

    public void run() {
        Properties props = getProperties();

        try (KafkaConsumer<String, ProductCreated> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList("product_events"));
            while (true) {
                ConsumerRecords<String, ProductCreated> records = consumer.poll(Duration.ofMillis(100));
                records.forEach(record -> {
                    System.out.printf("Received message: key=%s, value=%s%n",
                        record.key(), record.value());
                    port.storeProduct(record.value().getId().toString(), record.value().getName().toString());
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
        return props;
    }
}
