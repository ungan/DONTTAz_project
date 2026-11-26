package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.response.DailyTransactionAmount;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    @Query("""
            select t
            from Transaction t
            where t.account.accountId = :accountId
              and t.account.userId = :userId
              and (:startDate is null or t.transactionAt >= :startDate)
              and (:endDate is null or t.transactionAt < :endDate)
            order by t.transactionAt desc
            """)
    Page<Transaction> search(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    Optional<Transaction> findByIdAndAccountAccountIdAndAccountUserId(Long id, Long accountId, Long userId);

    @Query("""
            select t
            from Transaction t
            where t.account.accountId = :accountId
              and t.account.userId = :userId
              and t.txType in :txTypes
              and (:startDate is null or t.transactionAt >= :startDate)
              and (:endDate is null or t.transactionAt < :endDate)
            order by t.transactionAt desc
            """)
    Page<Transaction> searchByTxTypes(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("txTypes") Collection<String> txTypes,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    Optional<Transaction> findByAccountAccountIdAndAccountUserIdAndSourceTxId(Long accountId, Long userId, String sourceTxId);

    boolean existsByAccountAccountIdAndSourceAndSourceTxId(Long accountId, String source, String sourceTxId);

    @Query("""
            select t
            from Transaction t
            left join fetch TransactionAiMetadata m on m.transaction = t
            where t.userId = :userId
              and t.transactionAt >= :startDate
              and t.transactionAt < :endDate
            order by t.transactionAt desc
            """)
    Page<Transaction> searchWithAiMetadata(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // 번다운 차트용: 기간 내 지출을 DB에서 날짜별로 합산해 하루 한 행(한 달 최대 31행)만 돌려준다.
    // (user_id, transaction_at) 인덱스 범위를 그대로 쓰고, 거래 엔티티는 불러오지 않는다.
    // 입금은 지출이 아니므로 뺀다. 입금은 동기화 경로에서는 'DEPOSIT', 이체 경로에서는 원본 코드 '1'로 저장된다.
    @Query("""
            select new com.donttaz.donttazapi.account.dto.response.DailyTransactionAmount(
                cast(t.transactionAt as LocalDate), sum(t.amount))
            from Transaction t
            where t.userId = :userId
              and t.transactionAt >= :startDate
              and t.transactionAt < :endDate
              and upper(t.txType) not in ('DEPOSIT', '1')
            group by cast(t.transactionAt as LocalDate)
            """)
    List<DailyTransactionAmount> sumSpendingByDay(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
