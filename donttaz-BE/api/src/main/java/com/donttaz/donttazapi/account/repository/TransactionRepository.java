package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.domain.TransactionType;
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
            where t.account.id = :accountId
              and t.account.userId = :userId
              and (:type is null or t.type = :type)
              and (:startDate is null or t.transactionDate >= :startDate)
              and (:endDate is null or t.transactionDate < :endDate)
            order by t.transactionDate desc
            """)
    Page<Transaction> search(
            @Param("accountId") Long accountId,
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    Optional<Transaction> findByIdAndAccountIdAndAccountUserId(Long id, Long accountId, Long userId);
}
