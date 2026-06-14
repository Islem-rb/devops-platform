package com.devopsplatform.user_service;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private List<Map<String, Object>> users = new ArrayList<>(List.of(
        Map.of("id", 1, "name", "Islem", "email", "islem@devops.com"),
        Map.of("id", 2, "name", "Ahmed", "email", "ahmed@devops.com")
    ));

    @GetMapping
    public List<Map<String, Object>> getUsers() {
        return users;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable int id) {
        return users.stream()
            .filter(u -> u.get("id").equals(id))
            .findFirst()
            .orElse(Map.of("error", "User not found"));
    }
}
