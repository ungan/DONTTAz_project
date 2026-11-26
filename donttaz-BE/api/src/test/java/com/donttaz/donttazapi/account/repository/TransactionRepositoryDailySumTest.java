package com.donttaz.donttazapi.account.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.response.DailyTransactionAmount;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

/**
 * sumSpendingByDay(DB 날짜별 지출 집계)를 실제 DB(H2)로 검증한다.
 *
 * 1. 날짜 경계·사용자 범위는 기존 번다운 방식(searchWithAiMetadata 전량 조회 후 메모리 합산)과 같아야 한다.
 * 2. 입금은 지출이 아니므로 빠져야 한다. 입금은 동기화 경로에서 'DEPOSIT', 이체 경로에서 원본 코드 '1'로 저장된다.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryDailySumTest {

    private static final Set<String> DEPOSIT_TYPES = Set.of("DEPOSIT", "1");

    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired EntityManager em;

    private int seq = 0;

    @Test
    @DisplayName("날짜별 지출 집계: 자정·월 경계·다른 사용자는 기존 방식과 같고, 입금('DEPOSIT', '1')은 뺀다")
    void sumSpendingByDay_matchesLegacyGroupingWithoutDeposits() {
        Account mine = saveAccount(1L, "110-0000000001");
        Account other = saveAccount(2L, "110-0000000002");

        // 범위 경계: 시작 시각은 포함, 끝 시각(5/1 00:00)은 제외
        save(mine, 1L, "WITHDRAWAL", 1_000L, LocalDateTime.of(2026, 3, 31, 23, 59, 59));  // 제외 (이전 달)
        save(mine, 1L, "WITHDRAWAL", 2_000L, LocalDateTime.of(2026, 4, 1, 0, 0, 0));      // 포함 (시작 경계)
        save(mine, 1L, "2", 3_000L, LocalDateTime.of(2026, 4, 1, 12, 30));                // 포함 (이체 경로의 출금 코드)
        save(mine, 1L, "WITHDRAWAL", 4_000L, LocalDateTime.of(2026, 4, 1, 23, 59, 59));   // 포함 (같은 날 자정 직전)
        save(mine, 1L, "WITHDRAWAL", 5_000L, LocalDateTime.of(2026, 4, 2, 0, 0, 0));      // 포함 (다음 날 자정)
        save(mine, 1L, "DEPOSIT", 3_000_000L, LocalDateTime.of(2026, 4, 2, 9, 0));        // 제외 (월급, 동기화 경로)
        save(mine, 1L, "1", 50_000L, LocalDateTime.of(2026, 4, 15, 9, 0));                // 제외 (입금, 이체 경로 원본 코드)
        save(mine, 1L, "WITHDRAWAL", 6_000L, LocalDateTime.of(2026, 4, 30, 23, 59, 59));  // 포함 (마지막 날)
        save(mine, 1L, "WITHDRAWAL", 8_000L, LocalDateTime.of(2026, 5, 1, 0, 0, 0));      // 제외 (끝 경계)
        save(other, 2L, "WITHDRAWAL", 9_000L, LocalDateTime.of(2026, 4, 1, 10, 0));       // 제외 (다른 사용자)
        em.flush();
        em.clear();

        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 1, 0, 0);

        List<Transaction> legacyRows = transactionRepository
                .searchWithAiMetadata(1L, start, end, Pageable.unpaged()).getContent();

        // 기존 번다운 방식 그대로(입금 포함): 입금이 지출로 잡히던 문제를 보여 준다
        Map<LocalDate, Integer> legacyWithDeposits = groupByDay(legacyRows);
        assertThat(legacyWithDeposits.get(LocalDate.of(2026, 4, 2))).isEqualTo(3_005_000);

        // 기존 방식에서 입금만 뺀 값과 DB 집계가 같아야 한다
        Map<LocalDate, Integer> legacyWithoutDeposits = groupByDay(legacyRows.stream()
                .filter(tx -> !DEPOSIT_TYPES.contains(tx.getTxType().toUpperCase()))
                .toList());

        List<DailyTransactionAmount> rows = transactionRepository.sumSpendingByDay(1L, start, end);
        Map<LocalDate, Integer> aggregated = rows.stream()
                .collect(Collectors.toMap(DailyTransactionAmount::day, row -> row.totalAmount().intValue()));

        assertThat(aggregated).isEqualTo(legacyWithoutDeposits);
        assertThat(aggregated).containsExactlyInAnyOrderEntriesOf(Map.of(
                LocalDate.of(2026, 4, 1), 9_000,
                LocalDate.of(2026, 4, 2), 5_000,
                LocalDate.of(2026, 4, 30), 6_000));
        assertThat(rows).hasSize(3);
    }

    private Map<LocalDate, Integer> groupByDay(List<Transaction> transactions) {
        return transactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getTransactionAt().toLocalDate(),
                        Collectors.summingInt(tx -> tx.getAmount().intValue())));
    }

    private Account saveAccount(Long userId, String accountNumber) {
        return accountRepository.save(Account.builder()
                .userId(userId).bankCode("088").bankName("테스트은행")
                .accountNumber(accountNumber).balance(0L)
                .linkedAt(LocalDateTime.now()).isDeleted(false).isPrimary(true)
                .status(AccountStatus.ACTIVE).build());
    }

    private void save(Account account, Long userId, String txType, Long amount, LocalDateTime at) {
        transactionRepository.save(Transaction.builder()
                .account(account).userId(userId).source("BANK")
                .sourceTxId("tx-" + (seq++)).txType(txType).amount(amount)
                .balanceSnapshot(0L)
                .transactionAt(at)
                .syncedAt(at).build());
    }
}
