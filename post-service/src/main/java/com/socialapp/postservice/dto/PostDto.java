package com.socialapp.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class PostDto {

    @Data
    public static class CreatePostRequest {

        @NotNull(message = "userId is required")
        private Long userId;

        @NotBlank(message = "Content is required")
        @Size(max = 280, message = "Post content cannot exceed 280 characters")
        private String content;

        private String imageUrl;
    }

    @Data
    public static class UpdatePostRequest {
        @Size(max = 280, message = "Post content cannot exceed 280 characters")
        private String content;

        private String imageUrl;
    }

    @Data
    public static class PostResponse {
        private Long id;
        private Long userId;
        private String username;       // enriched from User Service
        private String userDisplayName; // enriched from User Service
        private String content;
        private String imageUrl;
        private String createdAt;
        private String updatedAt;
    }

    // Mirrors the UserResponse from user-service (used by WebClient deserialization)
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String displayName;
        private String bio;
        private String createdAt;
    }
}
