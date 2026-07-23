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

    /** 글쓴이가 빌려주는 입장(LEND)인지 빌리고 싶은 입장(BORROW)인지. 화면 표시/필터링 용도. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostDirection direction;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 거래 지역 (예: "홍익대학교 정문") */
    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    @Column(nullable = false)
    private Integer capacity;

    private LocalDateTime deadline;

    @Column(nullable = false)
    private String imageUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Post(User writer, PostCategory category, PostType type, PostDirection direction, String title,
                 String content, String location, Integer price, Integer capacity, LocalDateTime deadline,
                 String imageUrl) {
        this.writer = writer;
        this.category = category;
        this.type = type;
        this.direction = direction;
        this.title = title;
        this.content = content;
        this.location = location;
        this.price = price;
        this.status = PostStatus.OPEN;
        this.capacity = capacity;
        this.deadline = deadline;
        this.imageUrl = imageUrl;
    }

    public static Post create(User writer, PostCategory category, PostType type, PostDirection direction,
                               String title, String content, String location, Integer price, Integer capacity,
                               LocalDateTime deadline, String imageUrl) {
        return Post.builder()
                .writer(writer)
                .category(category)
                .type(type)
                .direction(direction)
                .title(title)
                .content(content)
                .location(location)
                .price(price)
                .capacity(capacity)
                .deadline(deadline)
                .imageUrl(imageUrl)
                .build();
    }

    /** 정원이 다 찼을 때 호출합니다. (Join API에서 사용) */
    public void close() {
        this.status = PostStatus.CLOSED;
    }

    /** 참여 취소 등으로 자리가 다시 생겼을 때 호출합니다. */
    public void open() {
        this.status = PostStatus.OPEN;
    }
}
