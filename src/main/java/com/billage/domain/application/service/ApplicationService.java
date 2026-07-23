package com.billage.domain.application.service;


import com.billage.domain.application.dto.response.ApplicationCancelResponse;
import com.billage.domain.application.entity.Application;
import com.billage.domain.application.entity.ApplicationStatus;
import com.billage.domain.application.repository.ApplicationRepository;
import com.billage.domain.credit.entity.CreditHistory;
import com.billage.domain.credit.entity.CreditReason;
import com.billage.domain.credit.repository.CreditHistoryRepository;
import com.billage.domain.post.entity.Post;
import com.billage.domain.user.entity.User;
import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    @Transactional
    public ApplicationCancelResponse cancel(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.APPLICATION_NOT_FOUND));

        User applicant = application.getApplicant();
        if (!applicant.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_APPLICATION_OWNER);
        }

        if (application.getStatus() == ApplicationStatus.CANCELED) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELED);
        }

        Post post = application.getPost();
        int refundAmount = post.getPrice();

        application.cancel();
        applicant.increaseCredit(refundAmount);
        creditHistoryRepository.save(
                CreditHistory.create(applicant, post, refundAmount, CreditReason.REFUND)
        );

        return ApplicationCancelResponse.of(application, refundAmount);
    }
}
