package com.billage.domain.user.service;

import com.billage.domain.user.dto.SchoolUpdateResponse;
import com.billage.domain.user.dto.UserMeResponse;
import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final SchoolCatalog schoolCatalog;

    public UserMeResponse getMe(Long userId) {
        return UserMeResponse.from(findUser(userId));
    }

    /** 온보딩에서 학교를 선택합니다. 목록에 없는 학교는 거부합니다. */
    @Transactional
    public SchoolUpdateResponse updateSchool(Long userId, String school) {
        if (!schoolCatalog.contains(school)) {
            throw new BusinessException(ErrorCode.INVALID_SCHOOL);
        }

        User user = findUser(userId);
        user.updateSchool(school);
        return SchoolUpdateResponse.from(user);
    }

    public List<String> searchSchools(String keyword) {
        return schoolCatalog.search(keyword);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
