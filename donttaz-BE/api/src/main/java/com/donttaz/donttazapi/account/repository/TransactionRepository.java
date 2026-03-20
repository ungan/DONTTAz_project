package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.ai.domain.TransactionAiMetadata;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t
            from Transaction t
            where t.account.accountId = :accountId
              and t.account.userId = :userId
              and (:txType is null or t.txType = :txType)
              and (:startDate is null or t.transactionAt >= :startDate)
              and (:endDate is null or t.transactionAt < :endDate)
            order by t.transactionAt desc
            """)
    Page<Transaction> search(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("txType") String txType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    Optional<Transaction> findByIdAndAccountAccountIdAndAccountUserId(Long id, Long accountId, Long userId);

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
}
