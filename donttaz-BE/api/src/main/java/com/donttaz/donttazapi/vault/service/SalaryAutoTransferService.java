package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryAutoTransferService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BankingClient bankingClient;
    private final AiPlanService aiPlanService;
    private final VaultCommandService vaultCommandService;

    /**
     * 매일 18:00에 실행되어 당일 월급 입금을 확인하고 금고로 자동 이체합니다.
     */
    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void executeSalaryAutoTransfer() {
        int todayDay = LocalDate.now().getDayOfMonth();
        log.info("월급 자동 이체 프로세스 시작 (급여일: {})", todayDay);

        // 1. 오늘이 급여일인 사용자 목록 조회
        List<User> salaryUsers = userRepository.findAllBySalaryDay(todayDay);
        int processedCount = 0;

        for (User user : salaryUsers) {
            try {
                processUserSalaryTransfer(user);
                processedCount++;
            } catch (Exception e) {
                log.error("사용자 월급 이체 처리 중 오류 발생: userId={}", user.getUserId(), e);
            }
        }

        log.info("월급 자동 이체 프로세스 종료 (총 {}명 처리 시도)", processedCount);
    }

    private void processUserSalaryTransfer(User user) {
        // 2. 대표계좌 조회 (isPrimary=true)
        Account primaryAccount = accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(
                user.getUserId(), AccountStatus.ACTIVE).orElse(null);

        if (primaryAccount == null) {
            log.warn("활성 대표계좌를 찾을 수 없습니다: userId={}", user.getUserId());
            return;
        }

        // 3. 당일 급여 입금 여부 확인 (Banking API)
        LocalDate today = LocalDate.now();
        TransactionQueryResult transactions = bankingClient.getTransactions(
                user.getUserId(),
                primaryAccount.getAccountNumber(),
                today,
                today,
                "DEPOSIT",
                1,
                100);

        boolean isSalaryDeposited = transactions.transactions().stream()
                .anyMatch(t -> t.counterpartyName().contains(user.getSalaryName()));

        if (!isSalaryDeposited) {
            log.info("오늘 급여 입금 내역이 없습니다: userId={}, salaryName={}", user.getUserId(), user.getSalaryName());
            return;
        }

        // 4. AI 소비 플랜 조회 (입금 예정 금액)
        String yearMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Long plannedAmount = aiPlanService.getPlannedDepositAmount(user.getUserId(), yearMonth);

        if (plannedAmount == null || plannedAmount <= 0) {
            log.info("금고 입금 예정 금액이 0원 이하입니다: userId={}", user.getUserId());
            return;
        }

        // 5. 금고 입금 실행
        vaultCommandService.depositInternal(user.getUserId(), primaryAccount.getAccountNumber(), plannedAmount);
        log.info("금고 자동 이체 성공: userId={}, amount={}", user.getUserId(), plannedAmount);
    }
}
