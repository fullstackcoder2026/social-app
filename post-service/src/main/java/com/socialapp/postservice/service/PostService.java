package com.socialapp.postservice.service;

import com.socialapp.postservice.dto.PostDto;
import com.socialapp.postservice.model.Post;
import com.socialapp.postservice.repository.PostRepository;
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
public class PostService {

    private final PostRepository postRepository;
    private final UserServiceClient userServiceClient;

    public PostDto.PostResponse createPost(PostDto.CreatePostRequest request) {
        log.info("Creating post for userId: {}", request.getUserId());

        // Validate user exists via User Service (inter-service call)
        PostDto.UserInfo user = userServiceClient.getUserById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "Cannot create post: user not found with id: " + request.getUserId()));

        Post post = Post.builder()
                .userId(request.getUserId())
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .build();

        Post saved = postRepository.save(post);
        log.info("Post created with id: {}", saved.getId());
        return toResponse(saved, user);
    }

    @Transactional(readOnly = true)
    public PostDto.PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        PostDto.UserInfo user = userServiceClient.getUserById(post.getUserId()).orElse(null);
        return toResponse(post, user);
    }

    @Transactional(readOnly = true)
    public List<PostDto.PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> {
                    PostDto.UserInfo user = userServiceClient.getUserById(post.getUserId()).orElse(null);
                    return toResponse(post, user);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostDto.PostResponse> getPostsByUser(Long userId) {
        PostDto.UserInfo user = userServiceClient.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(post -> toResponse(post, user))
                .collect(Collectors.toList());
    }

    public PostDto.PostResponse updatePost(Long id, PostDto.UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (request.getContent() != null) post.setContent(request.getContent());
        if (request.getImageUrl() != null) post.setImageUrl(request.getImageUrl());

        Post updated = postRepository.save(post);
        PostDto.UserInfo user = userServiceClient.getUserById(updated.getUserId()).orElse(null);
        return toResponse(updated, user);
    }

    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new RuntimeException("Post not found with id: " + id);
        }
        postRepository.deleteById(id);
        log.info("Deleted post with id: {}", id);
    }

    private PostDto.PostResponse toResponse(Post post, PostDto.UserInfo user) {
        PostDto.PostResponse response = new PostDto.PostResponse();
        response.setId(post.getId());
        response.setUserId(post.getUserId());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setCreatedAt(post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
        response.setUpdatedAt(post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null);
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setUserDisplayName(user.getDisplayName());
        }
        return response;
    }
}
