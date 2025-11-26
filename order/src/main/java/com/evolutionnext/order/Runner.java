package com.evolutionnext.order;

import com.evolutionnext.order.application.command.CreateOrder;
import com.evolutionnext.order.application.service.OrderApplicationService;
import com.evolutionnext.order.domain.aggregate.customer.CustomerId;
import com.evolutionnext.order.domain.aggregate.order.OrderId;
import com.evolutionnext.order.infrastructure.adapter.out.OrderEventKafkaPublisher;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        String stateString =
            "AK,AL,AZ,AR,CA,CO,CT,DE,FL,GA," +
            "HI,ID,IL,IN,IA,KS,KY,LA,ME,MD," +
            "MA,MI,MN,MS,MO,MT,NE,NV,NH,NJ," +
            "NM,NY,NC,ND,OH,OK,OR,PA,RI,SC," +
            "SD,TN,TX,UT,VT,VA,WA,WV,WI,WY";

        OrderEventKafkaPublisher orderEventKafkaPublisher = new OrderEventKafkaPublisher();
        OrderApplicationService orderApplicationService = new OrderApplicationService(orderEventKafkaPublisher);

        AtomicBoolean done = new AtomicBoolean(false);

        Random random = new Random();

        while (!done.get()) {
            String[] states = stateString.split(",");
            String state = states[random.nextInt(states.length)];
            int amount = random.nextInt(300) + 1;
            UUID productId = UUID.randomUUID();
            UUID customerId = UUID.randomUUID();
            int quantity = random.nextInt(20) + 1;

            orderApplicationService.submit(new CreateOrder(new OrderId(UUID.randomUUID()), new CustomerId(UUID.randomUUID())));

            Thread.sleep(random.nextInt(30000 - 1000 + 1) + 1000);
        }
    }
}
