package com.billage.domain.user.entity;

import com.billage.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(nullable = false, unique = true)
    private String socialId;

    @Column(nullable = false)
    private String nickname;

    private String profileImage;

    @Column(nullable = false)
    private Integer credit;

    private String school;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String email, String socialId, String nickname, String profileImage, String school) {
        this.email = email;
        this.socialId = socialId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.credit = 0;
        this.school = school;
    }

    public static User create(String email, String socialId, String nickname, String profileImage, String school) {
        return User.builder()
                .email(email)
                .socialId(socialId)
                .nickname(nickname)
                .profileImage(profileImage)
                .school(school)
                .build();
    }

    /**
     * 온보딩에서 학교를 설정합니다.
     * 값이 유효한 학교인지는 서비스 계층에서 검증합니다.
     */
    public void updateSchool(String school) {
        this.school = school;
    }

    /** 온보딩(학교 선택)을 마쳤는지 여부. */
    public boolean hasCompletedOnboarding() {
        return school != null;
    }

    /**
     * 크레딧을 증가시킵니다. (충전, 참여 취소 환불 등)
     */
    public void increaseCredit(int amount) {
        this.credit += amount;
    }

    /**
     * 크레딧을 차감합니다. (참여 신청 시 결제 등)
     * 잔액 부족 검증은 서비스 계층(ErrorCode.INSUFFICIENT_CREDIT)에서 수행합니다.
     */
    public void decreaseCredit(int amount) {
        this.credit -= amount;
    }
}
