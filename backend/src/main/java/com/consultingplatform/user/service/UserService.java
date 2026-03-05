package com.consultingplatform.user.service;

import com.consultingplatform.user.domain.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    
    /**
     * Create a user based on role (CLIENT, CONSULTANT, ADMIN)
     */
    User createUser(Map<String, Object> userData);
    
    /**
     * Get all users
     */
    List<User> getAllUsers();
    
    /**
     * Get user by ID
     */
    User getUserById(Long id);
    
    /**
     * Update user
     */
    User updateUser(Long id, Map<String, Object> userData);
    
    /**
     * Delete user
     */
    void deleteUser(Long id);
}
