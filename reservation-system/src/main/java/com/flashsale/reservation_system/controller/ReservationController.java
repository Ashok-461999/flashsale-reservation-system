package com.flashsale.reservation_system.controller;

import com.flashsale.reservation_system.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reserve")
public class ReservationController {

    @Autowired
    private ReservationService service;

    @PostMapping
    public String reserve(@RequestParam String productId) {
        return service.reserve(productId);
    }

    @PostMapping("/init")
    public String init(@RequestParam String productId, @RequestParam int quantity) {
        return service.initStock(productId,quantity);
    }

}