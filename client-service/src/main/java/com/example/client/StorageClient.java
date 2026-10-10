package com.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "storage-client", url = "${storage.service.url}")
public interface StorageClient {

    @GetMapping("/api/storage/hello")
    String hello();

    @PostMapping("/api/storage/items")
    Map<String, Object> saveItem(@RequestBody ItemRequest request);

    @GetMapping("/api/storage/items")
    List<Map<String, Object>> getItems();

    @PutMapping("/api/storage/items/{id}")
    Map<String, Object> updateItem(@PathVariable("id") Long id, @RequestBody ItemRequest request);

    @DeleteMapping("/api/storage/items/{id}")
    void deleteItem(@PathVariable("id") Long id);
}
