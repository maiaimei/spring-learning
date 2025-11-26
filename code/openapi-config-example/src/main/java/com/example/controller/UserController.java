package com.example.controller;

import com.example.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "User CRUD operations")
public class UserController {

  private final ConcurrentHashMap<BigDecimal, User> users = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @GetMapping
  @Operation(summary = "Get all users", description = "Retrieve all users")
  public List<User> getAllUsers() {
    return new ArrayList<>(users.values());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
  public User getUserById(@Parameter(description = "User ID") @PathVariable BigDecimal id) {
    User user = users.get(id);
    if (user == null) {
      throw new RuntimeException("User not found with id: " + id);
    }
    return user;
  }

  @PostMapping
  @Operation(summary = "Create user", description = "Create a new user")
  public User createUser(@RequestBody User user) {
    BigDecimal id = BigDecimal.valueOf(idGenerator.getAndIncrement());
    user.setId(id);
    users.put(id, user);
    return user;
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update user", description = "Update an existing user")
  public User updateUser(@Parameter(description = "User ID") @PathVariable BigDecimal id, @RequestBody User user) {
    if (!users.containsKey(id)) {
      throw new RuntimeException("User not found with id: " + id);
    }
    user.setId(id);
    users.put(id, user);
    return user;
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete user", description = "Delete a user by their ID")
  public void deleteUser(@Parameter(description = "User ID") @PathVariable BigDecimal id) {
    if (!users.containsKey(id)) {
      throw new RuntimeException("User not found with id: " + id);
    }
    users.remove(id);
  }
}