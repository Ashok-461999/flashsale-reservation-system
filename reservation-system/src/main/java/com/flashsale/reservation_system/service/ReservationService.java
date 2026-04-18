package com.flashsale.reservation_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {


    private String productId;
    private int quantity;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String reserve(String productId) {
        String key = "product:" + productId + ":stock";

        Long remaining = redisTemplate.opsForValue().decrement(key);

        if (remaining == null) {
            return "ERROR: Stock not initialized for product " + productId;
        }

        if (remaining >= 0) {
            return "SUCCESS: Reserved! Stock left: " + remaining;
        } else {
            // Rollback — undo the decrement
            redisTemplate.opsForValue().increment(key);
            return "SOLD OUT for product " + productId;
        }
    }


    public String initStock(String productId, int quantity) {
        String key = "product:" + productId + ":stock";
        redisTemplate.opsForValue().set(key, String.valueOf(quantity));
        return "Stock set to " + quantity + " for product " + productId;
    }
}