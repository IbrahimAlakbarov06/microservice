package com.example.storage;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final ItemRepository repository;

    public StorageController(ItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from storage-service";
    }

    @PostMapping("/items")
    public Item save(@RequestBody ItemRequest request) {
        return repository.save(new Item(request.name(), request.description()));
    }

    @GetMapping("/items")
    public List<Item> findAll() {
        return repository.findAll();
    }

    @PutMapping("/items/{id}")
    public Item update(@PathVariable Long id, @RequestBody ItemRequest r) {
        Item item = repository.findById(id).orElseThrow();
        item.setName(r.name());
        item.setDescription(r.description());
        return repository.save(item);
    }

    @DeleteMapping("/items/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
