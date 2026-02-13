package com.socialapp.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserDto {

    @Data
    public static class CreateUserRequest {

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Must be a valid email address")
        private String email;

        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        private String displayName;

        @Size(max = 250, message = "Bio cannot exceed 250 characters")
        private String bio;
    }

    @Data
    public static class UpdateUserRequest {

        @Size(max = 100, message = "Display name cannot exceed 100 characters")
        private String displayName;

        @Size(max = 250, message = "Bio cannot exceed 250 characters")
        private String bio;
    }

    @Data
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String displayName;
        private String bio;
        private String createdAt;
    }
}
