package com.consultingplatform.user.web;

import com.consultingplatform.user.domain.User;
import com.consultingplatform.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Create a user based on role type
     * Request body must include "role" field: "CLIENT", "CONSULTANT", or "ADMIN"
     */
    @PostMapping
    public User createUser(@RequestBody Map<String, Object> userData) {
        return userService.createUser(userData);
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        return userService.updateUser(id, userData);
    }
    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
