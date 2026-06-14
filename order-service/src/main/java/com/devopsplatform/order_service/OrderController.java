package com.devopsplatform.order_service;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private List<Map<String, Object>> orders = new ArrayList<>(List.of(
        Map.of("id", 1, "userId", 1, "productId", 2, "status", "CONFIRMED"),
        Map.of("id", 2, "userId", 2, "productId", 1, "status", "PENDING")
    ));

    @GetMapping
    public List<Map<String, Object>> getOrders() {
        return orders;
    }

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> order) {
        order = new HashMap<>(order);
        order.put("id", orders.size() + 1);
        order.put("status", "PENDING");
        orders.add(order);
        return order;
    }
}
