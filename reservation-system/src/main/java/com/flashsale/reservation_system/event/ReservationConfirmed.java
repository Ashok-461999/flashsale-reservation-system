package com.flashsale.reservation_system.event;

public record ReservationConfirmed(
        String reservationId,
        String productId,
        String userId,
        int quantity,
        long remainingStock,
        long timestamp
) {}