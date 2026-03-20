package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {

        List<Account> findByUserId(Long userId);

        List<Account> findAllByUserIdAndStatusAndIsDeletedFalse(Long userId, AccountStatus status);

        Optional<Account> findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(Long accountId, Long userId,
                        AccountStatus status);

        List<Account> findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(
                        Long userId,
                        String accountNumber,
                        AccountStatus status);

        Optional<Account> findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                        Long userId,
                        String bankCode,
                        String accountNumber,
                        AccountStatus status);

        Optional<Account> findByUserIdAndAccountNumberAndIsDeletedFalse(Long userId, String accountNumber);
        Optional<Account> findByAccountNumberAndIsDeletedFalse(String accountNumber); // 계좌 번호 기반 조회용
        Optional<Account> findFirstByAccountNumberAndStatusAndIsDeletedFalse(String accountNumber, AccountStatus status);
        Optional<Account> findByUserIdAndBankCodeAndAccountNumber(Long userId, String bankCode, String accountNumber);
        Optional<Account> findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(Long userId, AccountStatus status);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("update Account a set a.isPrimary = false where a.userId = :userId and a.isPrimary = true")
        void clearPrimaryByUserId(@Param("userId") Long userId);

        
}

