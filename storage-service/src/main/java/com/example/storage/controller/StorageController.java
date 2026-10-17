package com.example.storage.controller;

import com.example.storage.dto.ItemRequest;
import com.example.storage.model.Item;
import com.example.storage.model.ItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final ItemRepository repository;
    private final AtomicInteger counter = new AtomicInteger();

    public StorageController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from storage-service";
    }

    @PostMapping("/items")
    public Item save(@RequestBody ItemRequest r) {
        return repository.save(new Item(r.name(), r.description()));
    }

    @GetMapping("/items")
    public List<Item> findAll() {
        return repository.findAll();
    }

    @PutMapping("/items/{id}")
    public Item update(@PathVariable Long id, @RequestBody ItemRequest r) {
        Item item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id));
        item.setName(r.name());
        item.setDescription(r.description());
        return repository.save(item);
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found: " + id);
        }
        repository.deleteById(id);
    }

    @GetMapping("/flaky")
    public String flaky() {
        int n = counter.incrementAndGet();
        if (n % 3 != 0) throw new RuntimeException("Flaky failure, call #" + n);
        return "Flaky OK on call #" + n;
    }

    @GetMapping("/fail")
    public String fail() {
        throw new RuntimeException("Simulated failure");
    }
}