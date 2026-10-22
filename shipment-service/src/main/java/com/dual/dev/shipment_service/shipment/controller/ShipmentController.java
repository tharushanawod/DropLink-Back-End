package com.dual.dev.shipment_service.shipment.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShipmentController {
    @GetMapping("hello")
    public Map<String,String> hello(){

        return Map.of("Tharusha","Nawod");
    }
}
