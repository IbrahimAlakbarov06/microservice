package com.orderservice.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
@ConditionalOnProperty(name = "lab.mode", havingValue = "ordering", matchIfMissing = true)
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(topics = "orders", groupId = "order-group", concurrency = "3")
    public void onMessage(ConsumerRecord<String, String> record) throws InterruptedException {
        String[] p = record.value().split("\\|");
        if (p.length != 4) {
            return;
        }
        Thread.sleep(ThreadLocalRandom.current().nextInt(50));
        log.info("partition={} offset={} key={} customer={} type={} seq={}",
                record.partition(), record.offset(), record.key(), p[0], p[1], p[2]);
    }
}
