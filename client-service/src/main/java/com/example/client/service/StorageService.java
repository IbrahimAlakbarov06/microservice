package com.example.client.service;

import com.example.client.clients.StorageClient;
import com.example.client.dto.ItemRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Retry(name = "storage")
@CircuitBreaker(name = "storage")
public class StorageService {

    private final StorageClient client;

    public StorageService(StorageClient client) {
        this.client = client;
    }

    public String hello() { return client.hello(); }
    public Map<String, Object> save(ItemRequest r) { return client.saveItem(r); }
    public List<Map<String, Object>> all() { return client.getItems(); }
    public Map<String, Object> update(Long id, ItemRequest r) { return client.updateItem(id, r); }
    public void delete(Long id) { client.deleteItem(id); }
    public String flaky() { return client.flaky(); }
    public String fail() { return client.fail(); }
}