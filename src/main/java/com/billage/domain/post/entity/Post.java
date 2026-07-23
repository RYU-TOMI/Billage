package com.billage.domain.post.entity;

import com.billage.domain.user.entity.User;
import com.billage.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * capacity 는 RENTAL 타입이면 서비스 로직에서 항상 1로 강제 설정됩니다. (엔티티에는 강제 로직 없음)
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Column(nullable = false)
    private Integer capacity;

    private LocalDateTime deadline;

    private String rentalPeriod;

    @Column(nullable = false)
    private String imageUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Post(User writer, PostCategory category, PostType type, String title, String content,
                 Integer price, Integer capacity, LocalDateTime deadline, String rentalPeriod, String imageUrl) {
        this.writer = writer;
        this.category = category;
        this.type = type;
        this.title = title;
        this.content = content;
        this.price = price;
        this.status = PostStatus.OPEN;
        this.capacity = capacity;
        this.deadline = deadline;
        this.rentalPeriod = rentalPeriod;
        this.imageUrl = imageUrl;
    }

    public static Post create(User writer, PostCategory category, PostType type, String title, String content,
                               Integer price, Integer capacity, LocalDateTime deadline, String rentalPeriod, String imageUrl) {
        return Post.builder()
                .writer(writer)
                .category(category)
                .type(type)
                .title(title)
                .content(content)
                .price(price)
                .capacity(capacity)
                .deadline(deadline)
                .rentalPeriod(rentalPeriod)
                .imageUrl(imageUrl)
                .build();
    }
}