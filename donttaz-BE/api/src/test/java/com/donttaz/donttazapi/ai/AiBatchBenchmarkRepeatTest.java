package com.donttaz.donttazapi.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.response.DailyTransactionAmount;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * 번다운 차트 일별 합계 재측정 (2026-11-26).
 *
 * 세 경로를 비교한다.
 *   1. unpaged    : 기간 내 거래를 전부 엔티티로 불러와 메모리에서 합산 (프로젝트 당시 코드)
 *   2. paged500   : 500건씩 페이징하며 합산 (2026-06 로컬 개선안, 측정 결과 더 느렸음)
 *   3. aggregate  : DB에서 날짜별 SUM ... GROUP BY (2026-11-26 개선안, sumSpendingByDay)
 *
 * aggregate는 입금을 빼고 앞의 두 경로는 빼지 않는다. 시드는 전부 출금이라 세 경로의 합계가 같아야 한다.
 *
 * 기존 AiBatchBenchmarkTest는 1을 먼저 1회, 2를 나중에 1회 재서 워밍업이 뒤쪽에 유리했다.
 * 여기서는 세 경로를 먼저 워밍업한 뒤, 라운드마다 실행 순서를 돌려가며 5회씩 재고 중앙값을 낸다.
 * 실행한 SQL 문 수(Hibernate Statistics), 한 번에 받은 행 수, 세 경로의 결과 일치도 함께 확인한다.
 *
 * 거래 건수: -Dbench.tx=3000 (기본 3000)
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.show-sql=false",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener=WARN"
})
class AiBatchBenchmarkRepeatTest {

    static final int BATCH = 500;
    static final int WARMUP = 3;
    static final int ROUNDS = 5;
    static final long USER_ID = 1L;

    @Autowired AccountRepository accountRepository;
    @Autowired TransactionRepository transactionRepository;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;

    record Run(double ms, int maxRowsAtOnce, long statements, Map<LocalDate, Long> daily) {}

    interface Path {
        Run run(LocalDateTime start, LocalDateTime end, Statistics stats);
    }

    @Test
    @DisplayName("일별 합계: 전량 적재 vs 500 페이징 vs DB 집계, 워밍업 후 교차 5회")
    void measureRepeated() {
        int txCount = Integer.getInteger("bench.tx", 3000);
        seed(txCount);

        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = start.plusMinutes(txCount * 10L + 1);
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);

        String[] names = {"BEFORE unpaged", "PAGED  500", "AFTER  aggregate"};
        Path[] paths = {this::unpaged, this::paged, this::aggregate};

        for (int i = 0; i < WARMUP; i++) {
            for (Path path : paths) {
                path.run(start, end, stats);
            }
        }

        List<List<Run>> runs = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        for (int r = 0; r < ROUNDS; r++) {
            for (int k = 0; k < paths.length; k++) {
                int idx = (k + r) % paths.length; // 라운드마다 실행 순서를 한 칸씩 돌린다
                runs.get(idx).add(paths[idx].run(start, end, stats));
            }
        }

        System.out.println("\n========== 돈따즈 일별 합계 재측정 (H2, 거래 " + txCount + "건, 워밍업 " + WARMUP + "회 후 교차 " + ROUNDS + "회) ==========");
        for (int k = 0; k < paths.length; k++) {
            print(names[k], runs.get(k));
        }
        System.out.println("==============================================================================\n");

        Map<LocalDate, Long> expected = runs.get(0).get(0).daily();
        assertThat(expected.values().stream().mapToLong(Long::longValue).sum()).isEqualTo(txCount * 1000L);
        for (List<Run> pathRuns : runs) {
            assertThat(pathRuns).allMatch(run -> run.daily().equals(expected));
        }
        assertThat(runs.get(0)).allMatch(run -> run.maxRowsAtOnce() == txCount);
        assertThat(runs.get(1)).allMatch(run -> run.maxRowsAtOnce() == Math.min(BATCH, txCount));
        assertThat(runs.get(2)).allMatch(run -> run.maxRowsAtOnce() == expected.size());
    }

    private void seed(int txCount) {
        Account acc = accountRepository.save(Account.builder()
                .userId(USER_ID).bankCode("088").bankName("테스트은행")
                .accountNumber("110-0000000000").balance(0L)
                .linkedAt(LocalDateTime.now()).isDeleted(false).isPrimary(true)
                .status(AccountStatus.ACTIVE).build());
        LocalDateTime base = LocalDateTime.of(2026, 5, 1, 0, 0);
        List<Transaction> txs = new ArrayList<>(txCount);
        for (int i = 0; i < txCount; i++) {
            txs.add(Transaction.builder()
                    .account(acc).userId(USER_ID).source("BANK")
                    .sourceTxId("tx-" + i).txType("WITHDRAW").amount(1000L)
                    .balanceSnapshot(0L)
                    .transactionAt(base.plusMinutes(i * 10L))
                    .syncedAt(base).build());
        }
        transactionRepository.saveAll(txs);
        em.flush();
        em.clear();
    }

    private Run unpaged(LocalDateTime start, LocalDateTime end, Statistics stats) {
        em.clear();
        stats.clear();
        long t0 = System.nanoTime();
        List<Transaction> all = transactionRepository
                .searchWithAiMetadata(USER_ID, start, end, Pageable.unpaged()).getContent();
        Map<LocalDate, Long> daily = new HashMap<>();
        for (Transaction tx : all) {
            daily.merge(tx.getTransactionAt().toLocalDate(), tx.getAmount(), Long::sum);
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        return new Run(ms, all.size(), stats.getPrepareStatementCount(), daily);
    }

    private Run paged(LocalDateTime start, LocalDateTime end, Statistics stats) {
        em.clear();
        stats.clear();
        long t0 = System.nanoTime();
        Map<LocalDate, Long> daily = new HashMap<>();
        int pageNo = 0, maxAtOnce = 0;
        Page<Transaction> page;
        do {
            page = transactionRepository.searchWithAiMetadata(USER_ID, start, end, PageRequest.of(pageNo, BATCH));
            maxAtOnce = Math.max(maxAtOnce, page.getContent().size());
            for (Transaction tx : page.getContent()) {
                daily.merge(tx.getTransactionAt().toLocalDate(), tx.getAmount(), Long::sum);
            }
            pageNo++;
            em.clear();
        } while (page.hasNext());
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        return new Run(ms, maxAtOnce, stats.getPrepareStatementCount(), daily);
    }

    private Run aggregate(LocalDateTime start, LocalDateTime end, Statistics stats) {
        em.clear();
        stats.clear();
        long t0 = System.nanoTime();
        List<DailyTransactionAmount> rows = transactionRepository.sumSpendingByDay(USER_ID, start, end);
        Map<LocalDate, Long> daily = new HashMap<>();
        for (DailyTransactionAmount row : rows) {
            daily.put(row.day(), row.totalAmount());
        }
        double ms = (System.nanoTime() - t0) / 1_000_000.0;
        return new Run(ms, rows.size(), stats.getPrepareStatementCount(), daily);
    }

    private void print(String label, List<Run> runs) {
        List<Double> ms = runs.stream().map(Run::ms).sorted().toList();
        double median = ms.get(ms.size() / 2);
        StringBuilder each = new StringBuilder();
        for (Run run : runs) {
            each.append(String.format("%.1f ", run.ms()));
        }
        Run first = runs.get(0);
        System.out.printf("[%s] median=%.1f ms  min=%.1f  max=%.1f  | 회차별: %s| 한 번에 받은 행 최대 %d, SQL %d문%n",
                label, median, ms.get(0), ms.get(ms.size() - 1), each, first.maxRowsAtOnce(), first.statements());
    }
}
