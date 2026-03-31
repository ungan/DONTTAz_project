package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAutoTransferService {

    private final AccountRepository accountRepository;
    private final AiPlanService aiPlanService;
    private final VaultCommandService vaultCommandService;

    /**
     * 기존 매일 18:00 스케줄러 실행 로직 (급여 이체 API 연동으로 완전히 대체되어 비활성화함)
     */
    // @Scheduled(cron = "0 0 18 * * *")
    // @Transactional
    // public void executeSalaryAutoTransfer() {
    //     int todayDay = LocalDate.now().getDayOfMonth();
    //     log.info("월급 자동 이체 프로세스 시작 (급여일: {})", todayDay);
    // 
    //     List<User> salaryUsers = userRepository.findAllBySalaryDay(todayDay);
    //     int processedCount = 0;
    // 
    //     for (User user : salaryUsers) {
    //         try {
    //             processUserSalaryTransfer(user);
    //             processedCount++;
    //         } catch (Exception e) {
    //             log.error("사용자 월급 이체 처리 중 오류 발생: userId={}", user.getUserId(), e);
    //         }
    //     }
    // 
    //     log.info("월급 자동 이체 프로세스 종료 (총 {}명 처리 시도)", processedCount);
    // }

    /**
     * 특정 사용자 단일 건에 대해 당일 배정된 금고 입금 예정액을 이체 처리함
     */
    @Transactional
    public void processUserSalaryTransfer(User user) {
        // 2. 대표계좌 조회 (isPrimary=true)
        Account primaryAccount = accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(
                user.getUserId(), AccountStatus.ACTIVE).orElse(null);

        if (primaryAccount == null) {
            log.warn("활성 대표계좌를 찾을 수 없습니다: userId={}", user.getUserId());
            return;
        }

        // 3. AI 소비 플랜 조회 (입금 예정 금액 확인, sealedAmount 기반)
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Long plannedAmount = aiPlanService.getPlannedDepositAmount(user.getUserId(), yearMonth);

        if (plannedAmount == null || plannedAmount <= 0) {
            log.info("금고 입금 예정 금액이 0원 이하입니다: userId={}", user.getUserId());
            return;
        }

        // 4. 금고 입금 실행
        vaultCommandService.depositInternal(user.getUserId(), primaryAccount.getAccountNumber(), plannedAmount);
        log.info("금고 자동 이체 성공: userId={}, amount={}", user.getUserId(), plannedAmount);
    }
}
