package com.evolutionnext.order.customer.infrastructure.adapter.in;

import com.evolutionnext.customer.events.CustomerCreated;
import com.evolutionnext.order.customer.application.command.CustomerCommand;
import com.evolutionnext.order.customer.domain.aggregate.CustomerId;
import com.evolutionnext.order.customer.port.in.MessagingCustomerCommandPort;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

public class KafkaCustomerConsumer implements Runnable {

    private final MessagingCustomerCommandPort kafkaCustomerCommandPort;
    private static final Logger logger = LoggerFactory.getLogger(KafkaCustomerConsumer.class);
    private static final String TOPIC = "customers";

    public KafkaCustomerConsumer(MessagingCustomerCommandPort kafkaCustomerCommandPort) {
        this.kafkaCustomerCommandPort = kafkaCustomerCommandPort;
    }

    public void run() {
        Properties props = getProperties();
        try (KafkaConsumer<String, CustomerCreated> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(TOPIC));

            while (true) {
                consumer.poll(Duration.ofMillis(100))
                    .forEach(record -> {
                        logger.info("Received customer message: key={}, value={}", record.key(), record.value());

                        CustomerId customerId = new CustomerId(UUID.fromString(record.value().getCustomerId().toString()));
                        CharSequence firstName = record.value().getFirstName();
                        CharSequence lastName = record.value().getLastName();
                        String fullName = firstName + " " + lastName;
                        CustomerCommand customerCommand = new CustomerCommand.Create(customerId, fullName);

                        kafkaCustomerCommandPort.submit(customerCommand);
                    });
            }
        } catch (Exception e) {
            logger.error("Error while consuming messages", e);
        }
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "customer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        return props;
    }
}
