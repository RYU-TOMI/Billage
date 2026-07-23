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
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 승인 단계 없이 신청 즉시 CONFIRMED 로 생성됩니다.
 */
@Entity
@Table(name = "applications")
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

    /** 대여 신청 시 입력한 대여 일수. GROUP_BUY 참여에는 사용하지 않습니다. */
    private Integer rentalDays;

    /** 실제 결제된 총 금액. RENTAL 은 price * rentalDays, GROUP_BUY 는 price 그대로. */
    @Column(nullable = false)
    private Integer totalPrice;

    /** 수령 희망일 (선택 입력) */
    private LocalDate pickupDate;

    /** 대여 이유 (선택 입력, 결제 로직과 무관) */
    private String reason;

    /** 호스트에게 전달할 메시지 (선택 입력, 결제 로직과 무관) */
    private String message;

    @Builder(access = AccessLevel.PRIVATE)
    private Application(Post post, User applicant, Integer rentalDays, Integer totalPrice,
                         LocalDate pickupDate, String reason, String message) {
        this.post = post;
        this.applicant = applicant;
        this.status = ApplicationStatus.CONFIRMED;
        this.rentalDays = rentalDays;
        this.totalPrice = totalPrice;
        this.pickupDate = pickupDate;
        this.reason = reason;
        this.message = message;
    }

    public static Application create(Post post, User applicant, Integer rentalDays, Integer totalPrice,
                                      LocalDate pickupDate, String reason, String message) {
        return Application.builder()
                .post(post)
                .applicant(applicant)
                .rentalDays(rentalDays)
                .totalPrice(totalPrice)
                .pickupDate(pickupDate)
                .reason(reason)
                .message(message)
                .build();
    }

    public void cancel() {
        this.status = ApplicationStatus.CANCELED;
    }
}
