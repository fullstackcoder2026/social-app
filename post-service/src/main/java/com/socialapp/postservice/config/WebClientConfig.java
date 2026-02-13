package com.socialapp.postservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${user-service.url}")
    private String userServiceUrl;

    /**
     * WebClient bean pre-configured with the User Service base URL.
     * In Kubernetes, userServiceUrl resolves to the in-cluster DNS:
     *   http://user-service.social-app.svc.cluster.local:8080
     */
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(userServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
