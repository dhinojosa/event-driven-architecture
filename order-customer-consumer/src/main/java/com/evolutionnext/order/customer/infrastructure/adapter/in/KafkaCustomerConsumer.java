package com.evolutionnext.order.customer.infrastructure.adapter.in;

import com.evolutionnext.customer.events.CustomerCreatedMessage;
import com.evolutionnext.order.customer.application.command.CustomerCommand;
import com.evolutionnext.order.customer.application.result.CustomerCommandResult;
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
    private volatile boolean running = true;

    public KafkaCustomerConsumer(MessagingCustomerCommandPort kafkaCustomerCommandPort) {
        this.kafkaCustomerCommandPort = kafkaCustomerCommandPort;
    }

    public void run() {
        Properties props = getProperties();
        try (KafkaConsumer<String, CustomerCreatedMessage> consumer = new KafkaConsumer<>(props)) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
            consumer.subscribe(Collections.singletonList(TOPIC));
            while (running) {
                consumer.poll(Duration.ofMillis(100))
                    .forEach(record -> {
                        logger.info("Received customer message: key={}, value={}", record.key(), record.value());
                        CustomerId customerId = new CustomerId(UUID.fromString(record.value().getCustomerId().toString()));
                        CharSequence firstName = record.value().getFirstName();
                        CharSequence lastName = record.value().getLastName();
                        String fullName = String.format("%s %s", firstName, lastName);
                        CustomerCommand customerCommand = new CustomerCommand.Create(customerId, fullName);
                        CustomerCommandResult result = kafkaCustomerCommandPort.submit(customerCommand);
                        switch(result) {
                            case CustomerCommandResult.Created created -> logger.info("Customer {} created successfully", created.customerId());
                            case CustomerCommandResult.Error error -> logger.error("Error while creating customer: {}", error.message());
                        }
                    });
            }
        } catch (Exception e) {
            logger.error("Error while consuming messages", e);
        }
    }

    public void close() {
        running = false;
        logger.info("Shutting down consumer...");
    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-customer-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");
        props.put("specific.avro.reader", true);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }
}
