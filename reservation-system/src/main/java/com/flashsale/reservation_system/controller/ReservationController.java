package com.flashsale.reservation_system.controller;

import com.flashsale.reservation_system.service.ReservationService;
import com.flashsale.reservation_system.service.ReserveResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reserve")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReserveResult> reserve(
            @RequestParam String productId,
            @RequestParam(defaultValue = "anonymous") String userId) {
        ReserveResult result = service.reserve(productId, userId);
        HttpStatus status = switch (result.status()) {
            case SUCCESS         -> HttpStatus.OK;
            case SOLD_OUT        -> HttpStatus.CONFLICT;
            case NOT_INITIALIZED -> HttpStatus.NOT_FOUND;
            case ERROR           -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(result);
    }

    @PostMapping("/init")
    public ResponseEntity<String> init(@RequestParam String productId,
                                       @RequestParam int quantity) {
        service.initStock(productId, quantity);
        return ResponseEntity.ok("Stock set to " + quantity + " for " + productId);
    }
}