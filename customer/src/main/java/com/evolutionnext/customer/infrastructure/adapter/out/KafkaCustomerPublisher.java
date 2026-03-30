package com.evolutionnext.customer.infrastructure.adapter.out;

import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.events.CustomerEvent;
import com.evolutionnext.customer.events.CustomerCreatedMessage;
import com.evolutionnext.customer.port.out.CustomerPublisher;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;

public class KafkaCustomerPublisher implements CustomerPublisher {
    private final KafkaProducer<String, CustomerCreatedMessage> producer;
    private static final String TOPIC = "customers";

    public KafkaCustomerPublisher(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 20);
        props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public void publish(CustomerEvent customerEvent) {
        ProducerRecord<String, CustomerCreatedMessage> producerRecord =
            switch (customerEvent) {
                case CustomerEvent.Created(Customer customer) -> {
                    CustomerCreatedMessage message =
                        new CustomerCreatedMessage(customer.getId().id().toString(), Instant.now(),
                            customer.getFirstName(), customer.getLastName(),
                            customer.getEmail(), customer.getState());
                    yield new ProducerRecord<>(TOPIC, message.getState().toString(), message);
                }
            };

        try {
            producer.send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                    exception.printStackTrace();
                } else {
                    System.out.printf("Received message with key %s and value %s at offset %d%n",
                        producerRecord.key(), producerRecord.value(), metadata.offset());

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
