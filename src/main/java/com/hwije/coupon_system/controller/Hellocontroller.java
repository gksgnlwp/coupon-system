package com.hwije.coupon_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hellocontroller {

    @GetMapping("/hello")
    public String hello() {
        return "Hello Coupon Project!";
    }
}
