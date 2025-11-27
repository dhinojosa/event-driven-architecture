package com.evolutionnext.customer.infrastructure.adapter.out;

import com.evolutionnext.customer.domain.aggregate.Customer;
import com.evolutionnext.customer.domain.events.CustomerEvent;
import com.evolutionnext.customer.events.CustomerCreatedMessage;
import com.evolutionnext.customer.port.out.CustomerPublisher;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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
        props.put("schema.registry.url", schemaRegistryUrl);
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
            producer.send(producerRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
