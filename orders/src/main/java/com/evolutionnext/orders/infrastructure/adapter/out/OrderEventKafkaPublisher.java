package com.evolutionnext.orders.infrastructure.adapter.out;

import com.evolutionnext.orders.domain.aggregate.order.Order;
import com.evolutionnext.orders.domain.events.OrderEvent;
import com.evolutionnext.orders.domain.events.OrderPlaced;
import com.evolutionnext.orders.port.out.OrderEventPublisher;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Objects;
import java.util.Properties;

public class OrderEventKafkaPublisher implements OrderEventPublisher {

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            IntegerSerializer.class);
        return properties;
    }

    @Override
    public void publish(OrderEvent orderEvent) {
        if (Objects.requireNonNull(orderEvent) instanceof OrderPlaced(Order order)) {
            publishToKafka(order);
        } else {
            System.out.println("We are only sending new orders to Kafka at the moment");
        }
    }

    private void publishToKafka(Order order) {

    }
}
