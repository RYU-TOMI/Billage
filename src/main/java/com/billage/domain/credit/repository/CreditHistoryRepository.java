package com.billage.domain.credit.repository;

import com.billage.domain.credit.entity.CreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
}