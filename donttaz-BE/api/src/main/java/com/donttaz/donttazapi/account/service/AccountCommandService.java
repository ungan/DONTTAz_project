package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.AccountVerification;
import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.AccountVerificationRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountCommandService {

    private static final Long DEMO_USER_ID = 1L;
    private static final String LOCAL_DEV_CONSENT_TOKEN = "LOCAL-CONSENT-TOKEN";

    private static final Map<String, String> BANK_CODE_MAP = Map.ofEntries(
            Map.entry("003", "기업은행"),
            Map.entry("004", "KB국민은행"),
            Map.entry("011", "NH농협은행"),
            Map.entry("020", "우리은행"),
            Map.entry("081", "하나은행"),
            Map.entry("088", "신한은행"),
            Map.entry("090", "카카오뱅크")
    );

    private final AccountRepository accountRepository;
    private final AccountVerificationRepository accountVerificationRepository;
    private final Environment environment;

    public AccountCommandService(
            AccountRepository accountRepository,
            AccountVerificationRepository accountVerificationRepository,
            Environment environment
    ) {
        this.accountRepository = accountRepository;
        this.accountVerificationRepository = accountVerificationRepository;
        this.environment = environment;
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        Long userId = resolveCurrentUserId();
        validateConsentToken(userId, request);
        validateDuplicateAccount(userId, request.bankCode(), request.accountNumber());

        String bankName = BANK_CODE_MAP.get(request.bankCode());
        if (bankName == null) {
            throw new CustomException(ErrorCode.INVALID_BANK_CODE);
        }

        Account account = Account.builder()
                .userId(userId)
                .bankCode(request.bankCode())
                .bankName(bankName)
                .accountNumber(request.accountNumber())
                .accountAlias(blankToNull(request.accountAlias()))
                .balance(0)
                .linkedAt(LocalDateTime.now())
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);

        return CreateAccountResponse.builder()
                .accountId(savedAccount.getId())
                .bankName(savedAccount.getBankName())
                .balance(savedAccount.getBalance())
                .build();
    }

    private void validateConsentToken(Long userId, CreateAccountRequest request) {
        if (isLocalBypassToken(request.consentToken())) {
            return;
        }

        AccountVerification verification = accountVerificationRepository.findByConsentTokenAndVerifiedTrue(request.consentToken())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CONSENT_TOKEN));

        boolean mismatchedUser = !verification.getUserId().equals(userId);
        boolean mismatchedBankCode = !verification.getBankCode().equals(request.bankCode());
        boolean mismatchedAccountNumber = !verification.getAccountNumber().equals(request.accountNumber());
        boolean expired = verification.getExpiredAt().isBefore(LocalDateTime.now());

        if (mismatchedUser || mismatchedBankCode || mismatchedAccountNumber || expired) {
            throw new CustomException(ErrorCode.INVALID_CONSENT_TOKEN);
        }
    }

    private boolean isLocalBypassToken(String consentToken) {
        return environment.acceptsProfiles(Profiles.of("local"))
                && LOCAL_DEV_CONSENT_TOKEN.equals(consentToken);
    }

    private void validateDuplicateAccount(Long userId, String bankCode, String accountNumber) {
        boolean exists = accountRepository.existsByUserIdAndBankCodeAndAccountNumberAndStatus(
                userId,
                bankCode,
                accountNumber,
                AccountStatus.ACTIVE
        );

        if (exists) {
            throw new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Long resolveCurrentUserId() {
        // TODO: Replace with authenticated user resolution when auth is implemented.
        return DEMO_USER_ID;
    }
}
