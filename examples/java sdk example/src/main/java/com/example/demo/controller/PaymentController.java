package com.example.demo.controller;

import org.feature.management.client.annotation.Feature;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController {

    @GetMapping("/checkout")
    @Feature("DarkMode1")
    public Mono<String> processPayment() {
        return Mono.just("Checkout processed successfully! The new feature is enabled!");
    }
}
