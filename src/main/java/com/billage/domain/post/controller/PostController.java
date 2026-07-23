package com.billage.domain.post.controller;

import com.billage.domain.post.dto.PostCreateRequest;
import com.billage.domain.post.dto.PostResponse;
import com.billage.domain.post.dto.PostSummaryResponse;
import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostType;
import com.billage.domain.post.service.PostService;
import com.billage.global.common.response.ApiResponse;
import com.billage.global.common.response.PageResponse;
import com.billage.global.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(@AuthenticationPrincipal AuthUser authUser,
                                                 @Valid @RequestBody PostCreateRequest request) {
        return ApiResponse.success(postService.createPost(authUser.getUserId(), request));
    }

    @GetMapping
    public ApiResponse<PageResponse<PostSummaryResponse>> getPosts(@RequestParam(required = false) PostType type,
                                                                     @RequestParam(required = false) PostCategory category,
                                                                     @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(postService.getPosts(type, category, pageable));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<PostSummaryResponse>> searchPosts(@RequestParam String keyword,
                                                                        @RequestParam(required = false) PostType type,
                                                                        @RequestParam(required = false) PostCategory category,
                                                                        @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(postService.searchPosts(keyword, type, category, pageable));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long postId) {
        return ApiResponse.success(postService.getPost(postId));
    }
}
