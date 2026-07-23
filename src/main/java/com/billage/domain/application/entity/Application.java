package com.billage.domain.application.entity;

import com.billage.domain.post.entity.Post;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    // ↓↓↓ 여기 추가 (필드 선언 끝난 직후)
    @Builder(access = AccessLevel.PRIVATE)
    private Application(Post post, User applicant) {
        this.post = post;
        this.applicant = applicant;
        this.status = ApplicationStatus.CONFIRMED;
    }

    public static Application create(Post post, User applicant) {
        return Application.builder()
                .post(post)
                .applicant(applicant)
                .build();
    }

    public void cancel() {
        this.status = ApplicationStatus.CANCELED;
    }
}