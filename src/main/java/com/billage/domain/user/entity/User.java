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
}
