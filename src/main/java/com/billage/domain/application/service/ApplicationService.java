package com.billage.domain.application.service;

import com.billage.domain.application.dto.request.JoinRequest;
import com.billage.domain.application.dto.response.ApplicationCancelResponse;
import com.billage.domain.application.dto.response.ApplicationResponse;
import com.billage.domain.application.entity.Application;
import com.billage.domain.application.entity.ApplicationStatus;
import com.billage.domain.application.repository.ApplicationRepository;
import com.billage.domain.credit.entity.CreditHistory;
import com.billage.domain.credit.entity.CreditReason;
import com.billage.domain.credit.repository.CreditHistoryRepository;
import com.billage.domain.post.entity.Post;
import com.billage.domain.post.entity.PostStatus;
import com.billage.domain.post.entity.PostType;
import com.billage.domain.post.repository.PostRepository;
import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    /**
     * 대여/공동구매 참여 신청. 정원 체크, 결제 금액 계산, 크레딧 차감을 한 트랜잭션에서 처리합니다.
     *
     * <p>direction(LEND/BORROW)과 무관하게 크레딧은 항상 신청자(applicant)가 지불합니다.
     */
    @Transactional
    public ApplicationResponse join(Long postId, Long userId, JoinRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (post.getWriter().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.WRITER_CANNOT_APPLY);
        }

        if (applicationRepository.existsByPostIdAndApplicantIdAndStatus(postId, userId, ApplicationStatus.CONFIRMED)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        if (post.getDeadline() != null && post.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.DEADLINE_PASSED);
        }

        long currentCount = applicationRepository.countByPostIdAndStatus(postId, ApplicationStatus.CONFIRMED);
        if (post.getStatus() == PostStatus.CLOSED || currentCount >= post.getCapacity()) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }

        Integer rentalDays = null;
        int totalPrice;
        if (post.getType() == PostType.RENTAL) {
            if (request.rentalDays() == null || request.rentalDays() <= 0) {
                throw new BusinessException(ErrorCode.RENTAL_DAYS_REQUIRED);
            }
            rentalDays = request.rentalDays();
            // price/rentalDays 는 각각 DTO에서 상한이 걸려 있지만, long 으로 계산해서
            // 혹시라도 int 오버플로로 결제 금액이 음수가 되는 걸 한 번 더 막는다.
            // (음수가 되면 크레딧 체크를 통과해 버리고, 차감이 오히려 크레딧을 늘리게 된다)
            totalPrice = Math.toIntExact((long) post.getPrice() * rentalDays);
        } else {
            totalPrice = post.getPrice();
        }

        if (applicant.getCredit() < totalPrice) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_CREDIT);
        }

        Application application = Application.create(
                post, applicant, rentalDays, totalPrice,
                request.pickupDate(), request.reason(), request.message()
        );
        applicationRepository.save(application);

        applicant.decreaseCredit(totalPrice);
        creditHistoryRepository.save(CreditHistory.ofPayment(applicant, post, totalPrice));

        if (currentCount + 1 >= post.getCapacity()) {
            post.close();
        }

        return ApplicationResponse.from(application);
    }

    /**
     * 참여 취소 및 환불. 환불액은 실제 결제된 {@link Application#getTotalPrice()} 기준입니다.
     * (Post.price 를 다시 곱하면 RENTAL 대여 일수가 반영되지 않아 금액이 틀어집니다.)
     */
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
        int refundAmount = application.getTotalPrice();

        application.cancel();
        applicant.increaseCredit(refundAmount);
        creditHistoryRepository.save(
                CreditHistory.create(applicant, post, refundAmount, CreditReason.REFUND)
        );

        // 정원이 찬 뒤 취소되면 자리가 하나 생기므로 다시 모집 상태로 되돌린다.
        post.open();

        return ApplicationCancelResponse.of(application, refundAmount);
    }
}
