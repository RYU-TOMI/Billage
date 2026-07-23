package com.billage.global.security.oauth2;

import java.util.Map;

/**
 * 카카오 {@code /v2/user/me} 응답에서 필요한 값만 뽑아낸 것.
 *
 * <pre>
 * {
 *   "id": 123456789,
 *   "kakao_account": {
 *     "email": "user@kakao.com",              // 선택 동의. 거부하면 없음
 *     "profile": {
 *       "nickname": "현준",
 *       "profile_image_url": "https://..."
 *     }
 *   }
 * }
 * </pre>
 *
 * @param socialId     카카오 회원번호. 회원 식별 기준
 * @param email        이메일. 동의하지 않으면 null
 * @param nickname     닉네임. 없으면 기본값으로 대체됨
 * @param profileImage 프로필 이미지 주소. 없으면 null
 */
public record KakaoUserInfo(
        String socialId,
        String email,
        String nickname,
        String profileImage
) {

    private static final String DEFAULT_NICKNAME = "빌리지 이용자";

    @SuppressWarnings("unchecked")
    public static KakaoUserInfo from(Map<String, Object> attributes) {
        String socialId = String.valueOf(attributes.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return new KakaoUserInfo(socialId, null, DEFAULT_NICKNAME, null);
        }

        String email = (String) kakaoAccount.get("email");

        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        if (profile == null) {
            return new KakaoUserInfo(socialId, email, DEFAULT_NICKNAME, null);
        }

        // 닉네임은 User.nickname 이 NOT NULL 이라 비어 있으면 기본값으로 채웁니다.
        String nickname = (String) profile.get("nickname");
        if (nickname == null || nickname.isBlank()) {
            nickname = DEFAULT_NICKNAME;
        }

        return new KakaoUserInfo(
                socialId,
                email,
                nickname,
                (String) profile.get("profile_image_url")
        );
    }
}
