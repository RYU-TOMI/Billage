package com.billage.domain.auth.service;

import com.billage.domain.auth.dto.ReissueResponse;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import com.billage.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 리프레시 토큰으로 액세스 토큰을 다시 발급합니다.
     *
     * <p>액세스 토큰을 넣어 호출하면 거부합니다. 토큰 타입을 확인하지 않으면
     * 액세스 토큰만으로 무한히 갱신할 수 있게 됩니다.
     */
    public ReissueResponse reissue(String refreshToken) {
        if (!jwtTokenProvider.validate(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 탈퇴한 회원의 토큰으로는 재발급되지 않도록 확인합니다.
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return new ReissueResponse(jwtTokenProvider.createAccessToken(userId));
    }
}
