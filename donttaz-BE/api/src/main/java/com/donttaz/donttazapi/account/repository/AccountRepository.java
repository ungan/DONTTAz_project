package com.donttaz.donttazapi.account.repository;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUserIdAndStatus(Long userId, AccountStatus status);

    Optional<Account> findByIdAndUserIdAndStatus(Long id, Long userId, AccountStatus status);

    boolean existsByUserIdAndBankCodeAndAccountNumberAndStatus(
            Long userId,
            String bankCode,
            String accountNumber,
            AccountStatus status
    );
}
