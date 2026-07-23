package com.billage.domain.credit.service;



import com.billage.domain.credit.dto.request.CreditChargeRequest;
import com.billage.domain.credit.dto.response.CreditHistoryResponse;
import com.billage.domain.credit.dto.response.CreditResponse;
import com.billage.domain.credit.entity.CreditHistory;
import com.billage.domain.credit.entity.CreditReason;
import com.billage.domain.credit.repository.CreditHistoryRepository;
import com.billage.domain.user.entity.User;
import com.billage.domain.user.repository.UserRepository;
import com.billage.global.exception.BusinessException;
import com.billage.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditService {

    private final UserRepository userRepository;
    private final CreditHistoryRepository creditHistoryRepository;

    /** GET /api/users/me/credit 유저 크레딧 조회 */
    public CreditResponse getBalance(Long userId) {
        User user = getUser(userId);
        return CreditResponse.from(user.getCredit());
    }

    /** POST /api/users/me/credit/charge 크레딧 충전시 음수거나 0이면 오류 */
    @Transactional
    public CreditResponse charge(Long userId, CreditChargeRequest request) {
        if (request.amount() == null || request.amount() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }

        User user = getUser(userId);
        user.increaseCredit(request.amount());
        creditHistoryRepository.save(
                CreditHistory.create(user, null, request.amount(), CreditReason.CHARGE)
        );

        return CreditResponse.from(user.getCredit());
    }

    /** GET /api/users/me/credit/history?page=  creditHistory 최신순으로 정렬
     페이지를 조회한 뒤, 엔티티를 응답용 dto로 바꾸는 변환과정 코드 **/
    public CreditHistoryResponse.PageResponse getHistory(Long userId, Pageable pageable) {
        Page<CreditHistory> page =
                creditHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return CreditHistoryResponse.PageResponse.from(page);
    }

    /**유저 못찾으면 에러 발생 **/
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

}