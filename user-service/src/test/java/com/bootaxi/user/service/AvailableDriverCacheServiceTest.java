package com.bootaxi.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailableDriverCacheServiceTest {

    private static final String CACHE_KEY = "available-drivers";
    private static final Duration TTL = Duration.ofMinutes(5);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private AvailableDriverCacheService service;

    @BeforeEach
    void setUp() {
        service = new AvailableDriverCacheService(redisTemplate);
    }

    @Test
    void shouldParseDriverIdsFromCache() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn("10,20,30");

        List<Long> driverIds = service.read();

        assertEquals(List.of(10L, 20L, 30L), driverIds);
    }

    @Test
    void shouldReturnEmptyListWhenCacheIsBlank() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn("   ");

        assertTrue(service.read().isEmpty());
    }

    @Test
    void shouldWriteDriverIdsWithTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service.write(List.of(11L, 22L, 33L));

        verify(valueOperations).set(CACHE_KEY, "11,22,33", TTL);
    }

    @Test
    void shouldDeleteCacheEntryAndExposeDefaultPage() {
        service.evict();

        verify(redisTemplate).delete(CACHE_KEY);
        assertEquals(PageRequest.of(0, 10), service.page());
    }
}
