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

    @Builder
    public User(String email, String socialId, String nickname, String profileImage, String school) {
        this.email = email;
        this.socialId = socialId;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.credit = 0;
        this.school = school;
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
}
