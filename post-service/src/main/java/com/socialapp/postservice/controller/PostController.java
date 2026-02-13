package com.socialapp.postservice.controller;

import com.socialapp.postservice.dto.PostDto;
import com.socialapp.postservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    // POST /posts — Create a new post
    @PostMapping
    public ResponseEntity<PostDto.PostResponse> createPost(
            @Valid @RequestBody PostDto.CreatePostRequest request) {
        log.info("POST /posts - userId: {}", request.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    // GET /posts — List all posts (newest first)
    @GetMapping
    public ResponseEntity<List<PostDto.PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    // GET /posts/{id} — Get single post
    @GetMapping("/{id}")
    public ResponseEntity<PostDto.PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    // GET /posts/user/{userId} — All posts by a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDto.PostResponse>> getPostsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(postService.getPostsByUser(userId));
    }

    // PATCH /posts/{id} — Update post content
    @PatchMapping("/{id}")
    public ResponseEntity<PostDto.PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostDto.UpdatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    // DELETE /posts/{id} — Delete a post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
