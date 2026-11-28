package com.orderservice.store;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventStore {

    private final JdbcTemplate jdbc;

    public EventStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public boolean save(String eventId, String customer, String type, int seq,
                        int partition, long offset, boolean idempotent) {
        if (idempotent) {
            int inserted = jdbc.update(
                    "INSERT INTO processed_event(event_id) VALUES (?) ON CONFLICT DO NOTHING", eventId);
            if (inserted == 0) {
                return false;
            }
        }
        jdbc.update("INSERT INTO order_event(event_id, customer, type, seq, topic_partition, topic_offset) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                eventId, customer, type, seq, partition, offset);
        return true;
    }
}
