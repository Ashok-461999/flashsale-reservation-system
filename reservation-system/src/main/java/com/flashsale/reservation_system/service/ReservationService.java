package com.flashsale.reservation_system.service;

import com.flashsale.reservation_system.event.ReservationConfirmed;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReservationService {

    private static final long NOT_INITIALIZED_CODE = -2L;
    private static final long SOLD_OUT_CODE = -1L;
    private final KafkaTemplate <String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final DefaultRedisScript<Long> reserveScript;

    public ReservationService( KafkaTemplate < String, Object > kafkaTemplate, RedisTemplate<String, String> redisTemplate,
                               DefaultRedisScript<Long> reserveScript) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.reserveScript = reserveScript;
    }

    public ReserveResult reserve(String productId,String userId) {
        if (productId == null || productId.isBlank()) {
            return ReserveResult.error("productId must not be blank");
        }

        String key = stockKey(productId);
        Long result = redisTemplate.execute(reserveScript, List.of(key));

        if (result == null) {
            return ReserveResult.error("Redis returned null for product " + productId);
        }
        if (result == NOT_INITIALIZED_CODE) {
            return ReserveResult.notInitialized(productId);
        }
        if (result == SOLD_OUT_CODE) {
            return ReserveResult.soldOut(productId);
        }
        ReservationConfirmed event = new ReservationConfirmed( UUID.randomUUID ().toString (), productId,userId,1,result,System.currentTimeMillis() );
        kafkaTemplate.send("reservations", productId, event);

        return ReserveResult.success(productId, result);
    }

    public void initStock(String productId, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId must not be blank");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must be >= 0");
        }
        redisTemplate.opsForValue().set(stockKey(productId), String.valueOf(quantity));
    }

    private static String stockKey(String productId) {
        return "product:" + productId + ":stock";
    }
}