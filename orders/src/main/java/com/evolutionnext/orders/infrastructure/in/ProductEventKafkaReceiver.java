package com.evolutionnext.orders.infrastructure.in;


import com.evolutionnext.orders.port.in.InternalReceivingProductPort;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.*;

public class ProductEventKafkaReceiver {
    private final InternalReceivingProductPort internalReceivingProductPort;
    private final Properties properties;
    private final KafkaConsumer<String, String> consumer;

    public ProductEventKafkaReceiver(InternalReceivingProductPort internalReceivingProductPort) {
        this.internalReceivingProductPort = internalReceivingProductPort;
        this.properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "product-events-group");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(properties);
        this.consumer.subscribe(Collections.singletonList("product_events"));
    }

    public void run() {
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    internalReceivingProductPort.storeProduct(
                        UUID.fromString(record.key()),
                        record.value()
                    );
                }
            }
        } finally {
            consumer.close();
        }
    }
}
