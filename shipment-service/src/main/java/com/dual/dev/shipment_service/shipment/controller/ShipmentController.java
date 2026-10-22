package com.dual.dev.shipment_service.shipment.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class ShipmentController {
    @GetMapping("/hello")
    public Map<String,String> hello(){

        return Map.of("Tharusha","Nawod");
    }

    // Create shipment
    @PostMapping("/shipments")
     public String CreateShipments(){
        return "Shipment Created";
    }

    //Get all shipments
    @GetMapping("/shipments")
    public String GetShipments(){
        return "here are the shipments";
    }
    // Get shipment by ID
    @GetMapping("/shipments/{id}")
    public String GetShipment(){
        return "here are the shipment";
    }

    // Update shipment by ID
    @PutMapping("/shipments/{id}")
    public String UpdateShipment(){
        return "here are the shipment";
    }
    // Delete shipment
    @DeleteMapping("/shipments/{id}")
    public String DeleteShipment(){
        return "here are the shipment";
    }
}

