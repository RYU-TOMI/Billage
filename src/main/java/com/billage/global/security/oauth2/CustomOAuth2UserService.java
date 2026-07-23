package com.billage.global.security.oauth2;

import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 카카오 인증 성공 후 회원을 조회하거나 새로 만듭니다.
 *
 * <p>식별 기준은 {@code social_id} 입니다. 이메일은 사용자가 동의하지 않으면 넘어오지 않아
 * 식별 기준으로 쓸 수 없습니다.
 *
 * <p>신규 가입 시 {@code school} 은 비워둡니다. 프론트는 로그인 후
 * {@code GET /api/users/me} 의 {@code school} 이 null 이면 학교 선택 화면으로 보냅니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        KakaoUserInfo userInfo = KakaoUserInfo.from(oAuth2User.getAttributes());

        return userRepository.findBySocialId(userInfo.socialId())
                .map(user -> {
                    log.debug("기존 회원 로그인: userId={}", user.getId());
                    return new CustomOAuth2User(user.getId(), false, oAuth2User.getAttributes());
                })
                .orElseGet(() -> {
                    User created = register(userInfo);
                    log.info("신규 회원 가입: userId={}", created.getId());
                    return new CustomOAuth2User(created.getId(), true, oAuth2User.getAttributes());
                });
    }

    private User register(KakaoUserInfo userInfo) {
        return userRepository.save(User.create(
                userInfo.email(),
                userInfo.socialId(),
                userInfo.nickname(),
                userInfo.profileImage(),
                null                   // school 은 온보딩에서 선택
        ));
    }
}
