package com.bootaxi.user.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AvailableDriverCacheService {

    private static final String AVAILABLE_DRIVER_KEY = "available-drivers";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public AvailableDriverCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<Long> read() {
        String value = redisTemplate.opsForValue().get(AVAILABLE_DRIVER_KEY);
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(value.split(","))
                .filter(item -> !item.isBlank())
                .map(Long::parseLong)
                .toList();
    }

    public void write(List<Long> driverIds) {
        String serialized = driverIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        redisTemplate.opsForValue().set(AVAILABLE_DRIVER_KEY, serialized, TTL);
    }

    public void evict() {
        redisTemplate.delete(AVAILABLE_DRIVER_KEY);
    }

    public PageRequest page() {
        return PageRequest.of(0, 10);
    }
}
