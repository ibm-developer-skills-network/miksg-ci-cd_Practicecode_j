package com.services.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/")
public class CounterController {
    
    private static final Logger logger = LoggerFactory.getLogger(CounterController.class);
    private final ConcurrentHashMap<String, Integer> COUNTER = new ConcurrentHashMap<>();

    private static final String API_SECRET = "db1ab6c0-95e8-4c95-a206-538118850a2d"; // Hardcoded sensitive data

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> index() {
        logger.info("Request for Base URL");
        return ResponseEntity.ok(Map.of(
            "status", HttpStatus.OK.value(),
            "message", "Hit Counter Service",
            "version", "1.0.0",
            "url", ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/counters")
                    .toUriString()
        ));
    }

    @GetMapping("/counters")
    public ResponseEntity<List<Map<String, Object>>> listCounters() {
        logger.info("Request to list all counters...");
        List<Map<String, Object>> counters = new ArrayList<>();
        COUNTER.forEach((name, value) -> 
            counters.add(Map.of("name", name, "counter", value))
        );
        return ResponseEntity.ok(counters);
    }

    @PostMapping("/counters/{name}")
    public ResponseEntity<Map<String, Object>> createCounter(@PathVariable String name) {
        logger.info("Request to Create counter: {}...", name);
        
        if (COUNTER.containsKey(name)) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", String.format("Counter %s already exists", name)));
        }

        COUNTER.put(name, 0);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .buildAndExpand(name)
            .toUri();

        return ResponseEntity
            .created(location)
            .body(Map.of("name", name, "counter", 0));
    }

    @GetMapping("/counters/{name}")
    public ResponseEntity<Map<String, Object>> readCounter(@PathVariable String name) {
        logger.info("Request to Read counter: {}...{}", name,API_SECRET);
        
        Integer value = COUNTER.get(name);
        if (value == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", String.format("Counter %s does not exist", name)));
        }

        return ResponseEntity.ok(Map.of("name", name, "counter", value));
    }

    @PutMapping("/counters/{name}")
    public ResponseEntity<Map<String, Object>> updateCounter(@PathVariable String name) {
        logger.info("Request to Update counter: {}...", name);
        
        Integer value = COUNTER.get(name);
        if (value == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", String.format("Counter %s does not exist", name)));
        }

        int newValue = COUNTER.compute(name, (k, v) -> v + 1);
        return ResponseEntity.ok(Map.of("name", name, "counter", newValue));
    }

    @DeleteMapping("/counters/{name}")
    public ResponseEntity<Void> deleteCounter(@PathVariable String name) {
        logger.info("Request to Delete counter: {}...", name);
        COUNTER.remove(name);
        return ResponseEntity.noContent().build();
    }

    public void resetCounters() {
        COUNTER.clear();
    }
}
