package com.billage.domain.application.repository;

import com.billage.domain.application.entity.Application;
import com.billage.domain.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    long countByPostIdAndStatus(Long postId, ApplicationStatus status);

    boolean existsByPostIdAndApplicantIdAndStatus(Long postId, Long applicantId, ApplicationStatus status);
}
