package com.billage.domain.user.dto;

import com.billage.domain.user.entity.User;

public record SchoolUpdateResponse(Long id, String school) {

    public static SchoolUpdateResponse from(User user) {
        return new SchoolUpdateResponse(user.getId(), user.getSchool());
    }
}
