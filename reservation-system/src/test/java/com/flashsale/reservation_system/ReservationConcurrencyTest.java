package com.flashsale.reservation_system;

import com.flashsale.reservation_system.service.ReservationService;
import com.flashsale.reservation_system.service.ReserveResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ReservationConcurrencyTest {

    @Autowired
    ReservationService service;

    @Autowired
    @Qualifier("redisTemplate")
    RedisTemplate<String, String> redis;

    @Test
    void noOverselling() throws Exception {
        String productId = "test-product";
        int stock = 100;
        int totalRequests = 1000;

        // Step 1: seed stock
        service.initStock(productId, stock);

        // Step 2-4: setup
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(totalRequests);
        AtomicInteger successes = new AtomicInteger(0);
        AtomicInteger soldOut = new AtomicInteger(0);

        // Step 5: submit 1000 tasks
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    startGate.await();
                    ReserveResult result = service.reserve(productId,"test");
                    if (result.status() == ReserveResult.Status.SUCCESS) {
                        successes.incrementAndGet();
                    } else if (result.status() == ReserveResult.Status.SOLD_OUT) {
                        soldOut.incrementAndGet();
                    }
                } catch (Exception e) {
                    // ignore
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Step 6: fire the starting gun
        startGate.countDown();

        // Step 7: wait for all threads to finish
        assertTrue(doneGate.await(30, TimeUnit.SECONDS), "tasks did not finish in 30s");

        // Step 8: shutdown
        executor.shutdown();

        // Step 9: read final stock
        String finalStock = redis.opsForValue().get("product:" + productId + ":stock");

        // Step 10: assertions
        assertEquals(stock, successes.get(), "expected exactly " + stock + " successes");
        assertEquals(totalRequests - stock, soldOut.get(), "expected " + (totalRequests - stock) + " sold-out");
        assertEquals("0", finalStock, "Redis stock must end at exactly 0");

        // Step 11: cleanup
        redis.delete("product:" + productId + ":stock");
    }
}