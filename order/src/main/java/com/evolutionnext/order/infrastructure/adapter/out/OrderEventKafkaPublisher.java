package com.evolutionnext.order.infrastructure.adapter.out;

import com.evolutionnext.order.domain.aggregate.order.Order;
import com.evolutionnext.order.domain.events.OrderEvent;
import com.evolutionnext.order.events.*;
import com.evolutionnext.order.port.out.OrderEventPublisher;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Properties;

public class OrderEventKafkaPublisher implements OrderEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventKafkaPublisher.class);
    public static final String TOPIC = "orders";
    private final KafkaProducer<String, OrderEventMessage> producer;

    public OrderEventKafkaPublisher(KafkaProducer<String, OrderEventMessage> producer) {
        this.producer = producer;
    }

    public OrderEventKafkaPublisher(String bootstrapServers, String schemaRegistryUrl) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.put("schema.registry.url", schemaRegistryUrl);
        properties.put("auto.register.schemas", "false");
        properties.put("use.latest.version", "true");
        properties.put("latest.compatibility.strict", "false");
        this.producer = new KafkaProducer<>(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Closing Kafka Producer");
            producer.close();
        }));
    }


    @Override
    public void publish(OrderEvent orderEvent) {
        switch (orderEvent) {
            case OrderEvent.OrderPlaced(Order order) -> {
                logger.info("Publishing order placed event: {}", order);
                OrderEventMessage orderEventMessage = new OrderEventMessage(
                    order.getOrderId().id(),
                    Instant.now(),
                    EventType.ORDER_PLACED,
                    new OrderPlacedMessage(
                        new OrderMessage(order.getCustomerId().uuid(),
                            order.getOrderItems().stream().map(orderItem ->
                                new OrderItemMessage(
                                    orderItem.productId().id(),
                                    orderItem.quantity(),
                                    orderItem.price().doubleValue())).toList()
                        )));
                ProducerRecord<String, OrderEventMessage> orderEventProducerRecord =
                    new ProducerRecord<>("orders", order.getOrderId().id().toString(), orderEventMessage);
                producer.send(orderEventProducerRecord);
            }
            case OrderEvent.OrderCancelled(var orderId, var reason) -> {
                logger.info("Publishing order cancelled event: {}", orderId);
                OrderEventMessage orderEventMessage = new OrderEventMessage(
                    orderId.id(),
                    Instant.now(),
                    EventType.ORDER_CANCELLED,
                    new OrderCancelledMessage(reason));
                ProducerRecord<String, OrderEventMessage> orderEventProducerRecord =
                    new ProducerRecord<>(TOPIC, orderId.id().toString(),
                        orderEventMessage);
                producer.send(orderEventProducerRecord);
            }
            default -> logger.warn("Event not published: {}", orderEvent);
        }
    }
}
