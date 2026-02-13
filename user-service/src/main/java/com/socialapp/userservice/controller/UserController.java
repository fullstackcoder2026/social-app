package com.socialapp.userservice.controller;

import com.socialapp.userservice.dto.UserDto;
import com.socialapp.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // POST /users — Register a new user
    @PostMapping
    public ResponseEntity<UserDto.UserResponse> createUser(
            @Valid @RequestBody UserDto.CreateUserRequest request) {
        log.info("POST /users - creating user: {}", request.getUsername());
        UserDto.UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /users — List all users
    @GetMapping
    public ResponseEntity<List<UserDto.UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // GET /users/{id} — Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // GET /users/username/{username} — Get user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto.UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }

    // PATCH /users/{id} — Update user profile
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto.UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto.UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // DELETE /users/{id} — Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
