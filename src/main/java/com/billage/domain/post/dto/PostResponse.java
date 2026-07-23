package com.billage.domain.post.dto;

import com.billage.domain.post.entity.Post;
import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostDirection;
import com.billage.domain.post.entity.PostStatus;
import com.billage.domain.post.entity.PostType;
import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long writerId,
        String writerNickname,
        PostCategory category,
        PostType type,
        PostDirection direction,
        String title,
        String content,
        String location,
        Integer price,
        PostStatus status,
        Integer capacity,
        LocalDateTime deadline,
        String imageUrl,
        LocalDateTime createdAt
) {

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getWriter().getId(),
                post.getWriter().getNickname(),
                post.getCategory(),
                post.getType(),
                post.getDirection(),
                post.getTitle(),
                post.getContent(),
                post.getLocation(),
                post.getPrice(),
                post.getStatus(),
                post.getCapacity(),
                post.getDeadline(),
                post.getImageUrl(),
                post.getCreatedAt()
        );
    }
}
