package com.billage.domain.credit.repository;

import com.billage.domain.credit.entity.CreditHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
    Page<CreditHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}