package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdAndStatusAndIsDeletedFalse(Long userId, AccountStatus status);

    Optional<Account> findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(Long accountId, Long userId, AccountStatus status);

    Optional<Account> findByUserIdAndBankCodeAndAccountNumber(Long userId, String bankCode, String accountNumber);

    boolean existsByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
            Long userId,
            String bankCode,
            String accountNumber,
            AccountStatus status
    );
}
