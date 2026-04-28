package com.flashsale.reservation_system.service;

public record ReserveResult(Status status, String productId, Long remainingStock, String message) {

    public enum Status {
        SUCCESS,
        SOLD_OUT,
        NOT_INITIALIZED,
        ERROR
    }

    public static ReserveResult success(String productId, long remaining) {
        return new ReserveResult(Status.SUCCESS, productId, remaining, null);
    }

    public static ReserveResult soldOut(String productId) {
        return new ReserveResult(Status.SOLD_OUT, productId, 0L, null);
    }

    public static ReserveResult notInitialized(String productId) {
        return new ReserveResult(Status.NOT_INITIALIZED, productId, null,
                "Stock not initialized for " + productId);
    }

    public static ReserveResult error(String msg) {
        return new ReserveResult(Status.ERROR, null, null, msg);
    }
}