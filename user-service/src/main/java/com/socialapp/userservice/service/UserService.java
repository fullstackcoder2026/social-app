package com.socialapp.userservice.service;

import com.socialapp.userservice.dto.UserDto;
import com.socialapp.userservice.model.User;
import com.socialapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserDto.UserResponse createUser(UserDto.CreateUserRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
                .bio(request.getBio())
                .build();

        User saved = userRepository.save(user);
        log.info("User created with id: {}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserDto.UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto.UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public UserDto.UserResponse updateUser(Long id, UserDto.UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getDisplayName() != null) user.setDisplayName(request.getDisplayName());
        if (request.getBio() != null) user.setBio(request.getBio());

        return toResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    private UserDto.UserResponse toResponse(User user) {
        UserDto.UserResponse response = new UserDto.UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setDisplayName(user.getDisplayName());
        response.setBio(user.getBio());
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        return response;
    }
}
