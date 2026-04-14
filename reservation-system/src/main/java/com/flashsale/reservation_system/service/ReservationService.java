package com.flashsale.reservation_system.service;

import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    public String reserve(String productId) {
        return "Service Working for product " + productId;
    }
}