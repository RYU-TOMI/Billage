package com.billage.domain.post.dto;

import com.billage.domain.post.entity.Post;
import com.billage.domain.post.entity.PostCategory;
import com.billage.domain.post.entity.PostDirection;
import com.billage.domain.post.entity.PostStatus;
import com.billage.domain.post.entity.PostType;
import java.time.LocalDateTime;

/** 목록 · 검색 응답용 요약 정보. 본문 전체(content)는 포함하지 않습니다. */
public record PostSummaryResponse(
        Long id,
        String title,
        PostCategory category,
        PostType type,
        PostDirection direction,
        String location,
        Integer price,
        PostStatus status,
        String imageUrl,
        LocalDateTime createdAt
) {

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getCategory(),
                post.getType(),
                post.getDirection(),
                post.getLocation(),
                post.getPrice(),
                post.getStatus(),
                post.getImageUrl(),
                post.getCreatedAt()
        );
    }
}
