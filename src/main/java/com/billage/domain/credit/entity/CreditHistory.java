package com.billage.domain.credit.entity;

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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * amount 는 양수(충전 · 환불) / 음수(결제-차감) 로 부호를 구분합니다.
 */
@Entity
@Table(name = "credit_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_post_id")
    private Post relatedPost;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditReason reason;

    @Builder(access = AccessLevel.PRIVATE)
    private CreditHistory(User user, Post relatedPost, Integer amount, CreditReason reason) {
        this.user = user;
        this.relatedPost = relatedPost;
        this.amount = amount;
        this.reason = reason;
    }

    public static CreditHistory create(User user, Post relatedPost, Integer amount, CreditReason reason) {
        return CreditHistory.builder()
                .user(user)
                .relatedPost(relatedPost)
                .amount(amount)
                .reason(reason)
                .build();
    }


    public static CreditHistory ofCharge(User user, int amount) {
        return CreditHistory.builder()
                .user(user)
                .relatedPost(null)
                .amount(amount)
                .reason(CreditReason.CHARGE)
                .build();
    }

    public static CreditHistory ofPayment(User user, Post post, int amount) {
        return CreditHistory.builder()
                .user(user)
                .relatedPost(post)
                .amount(-Math.abs(amount))
                .reason(CreditReason.PAYMENT)
                .build();
    }

    public static CreditHistory ofRefund(User user, Post post, int amount) {
        return CreditHistory.builder()
                .user(user)
                .relatedPost(post)
                .amount(Math.abs(amount))
                .reason(CreditReason.REFUND)
                .build();
    }
}
