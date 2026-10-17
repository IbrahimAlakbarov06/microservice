package com.example.client.controller;

import com.example.client.dto.ItemRequest;
import com.example.client.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final StorageService service;

    public ClientController(StorageService service) {
        this.service = service;
    }

    @GetMapping("/hello")
    public String hello() {
        return "client-service -> " + service.hello();
    }

    @PostMapping("/items")
    public Map<String, Object> create(@RequestBody ItemRequest r) {
        return service.save(process(r));
    }

    @GetMapping("/items")
    public List<Map<String, Object>> all() {
        return service.all();
    }

    @PutMapping("/items/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody ItemRequest r) {
        return service.update(id, process(r));
    }

    @DeleteMapping("/items/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Item " + id + " deleted";
    }

    @GetMapping("/flaky")
    public String flaky() {
        return service.flaky();
    }

    @GetMapping("/fail")
    public String fail() {
        return service.fail();
    }

    private ItemRequest process(ItemRequest r) {
        if (r.name() == null || r.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be empty");
        }
        return new ItemRequest(r.name().trim().toUpperCase(),
                r.description() == null ? "" : r.description().trim());
    }
}