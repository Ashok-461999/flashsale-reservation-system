package com.flashsale.reservation_system.controller;

import com.flashsale.reservation_system.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserve")
public class ReservationController {

    @Autowired
    private ReservationService service;

    @GetMapping
    public String reserve(@RequestParam String productId) {
        return service.reserve(productId);
    }
}