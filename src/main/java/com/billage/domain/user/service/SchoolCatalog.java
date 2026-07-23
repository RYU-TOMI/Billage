package com.billage.domain.user.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 선택 가능한 학교 목록.
 *
 * <p>데모 범위에서는 별도 테이블 없이 하드코딩 목록으로 운영합니다.
 * 학교를 늘려야 하면 {@link #SCHOOLS} 에 추가하면 됩니다.
 */
@Component
public class SchoolCatalog {

    private static final List<String> SCHOOLS = List.of(
            "홍익대학교",
            "연세대학교",
            "이화여자대학교",
            "서강대학교",
            "중앙대학교",
            "숙명여자대학교",
            "한양대학교",
            "건국대학교",
            "국민대학교",
            "명지대학교"
    );

    /** 전체 목록. */
    public List<String> findAll() {
        return SCHOOLS;
    }

    /** 이름에 keyword 가 포함된 학교만 추립니다. keyword 가 비어 있으면 전체를 돌려줍니다. */
    public List<String> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return SCHOOLS;
        }
        String trimmed = keyword.trim();
        return SCHOOLS.stream()
                .filter(school -> school.contains(trimmed))
                .toList();
    }

    /** 목록에 있는 학교인지 확인합니다. */
    public boolean contains(String school) {
        return SCHOOLS.contains(school);
    }
}
