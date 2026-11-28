package com.orderservice.consumer;

import com.orderservice.store.EventStore;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "lab.mode", havingValue = "db")
public class OrderDbConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderDbConsumer.class);

    private final EventStore store;

    @Value("${lab.crash:false}")
    private boolean crash;

    @Value("${lab.idempotent:false}")
    private boolean idempotent;

    public OrderDbConsumer(EventStore store) {
        this.store = store;
    }

    @KafkaListener(topics = "orders-db", groupId = "order-db-group", containerFactory = "manualAckFactory")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String[] p = record.value().split("\\|");
        if (p.length != 4) {
            ack.acknowledge();
            return;
        }

        boolean saved = store.save(p[3], p[0], p[1], Integer.parseInt(p[2]),
                record.partition(), record.offset(), idempotent);

        if (!saved) {
            log.info("SKIPPED duplicate eventId={} partition={} offset={}",
                    p[3], record.partition(), record.offset());
            ack.acknowledge();
            return;
        }

        log.info("SAVED partition={} offset={} customer={} type={} seq={} eventId={}",
                record.partition(), record.offset(), p[0], p[1], p[2], p[3]);

        if (crash) {
            log.warn("CRASH: halt() after the save, before the ack (partition={} offset={})",
                    record.partition(), record.offset());
            Runtime.getRuntime().halt(1);
        }

        ack.acknowledge();
        log.info("ACKED partition={} offset={}", record.partition(), record.offset());
    }
}
