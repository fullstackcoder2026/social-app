package com.socialapp.postservice.service;

import com.socialapp.postservice.dto.PostDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

/**
 * Client for calling User Service via reactive WebClient.
 * Inter-service communication goes through Kubernetes internal DNS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient userServiceWebClient;

    /**
     * Fetch user info by ID. Returns empty if user is not found (404).
     * Throws for any other error.
     */
    public Optional<PostDto.UserInfo> getUserById(Long userId) {
        log.debug("Fetching user info for userId: {}", userId);
        try {
            PostDto.UserInfo user = userServiceWebClient.get()
                    .uri("/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(PostDto.UserInfo.class)
                    .block(); // blocking call — acceptable in MVC context
            return Optional.ofNullable(user);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("User not found for id: {}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error calling user-service for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Unable to reach user-service: " + e.getMessage(), e);
        }
    }
}
