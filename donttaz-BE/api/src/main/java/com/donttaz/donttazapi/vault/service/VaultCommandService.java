package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.request.CreateVaultRequest;
import com.donttaz.donttazapi.vault.dto.request.SetWishlistGoalRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest;
import com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultSummaryResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultWithdrawResponse;
import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.vault.repository.VaultRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 금고(Vault) 관련 상태 변경(Command)을 처리하는 서비스.
 * 금고 생성, 입금, 출금, 위시리스트 목표 설정 및 달성 등의 비즈니스 로직을 수행합니다.
 * CQRS 패턴에 따라 조회(Query) 서비스와 분리되어 있습니다.
 */
@Service
@Transactional
public class VaultCommandService {

    private static final Logger log = LoggerFactory.getLogger(VaultCommandService.class);

    private final VaultRepository vaultRepository;
    private final AccountRepository accountRepository;
    private final BankingClient bankingClient;
    private final CurrentUserProvider currentUserProvider;

    public VaultCommandService(
            VaultRepository vaultRepository,
            AccountRepository accountRepository,
            BankingClient bankingClient,
            CurrentUserProvider currentUserProvider) {
        this.vaultRepository = vaultRepository;
        this.accountRepository = accountRepository;
        this.bankingClient = bankingClient;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * 새로운 금고(DEFAULT 또는 WISHLIST)를 생성합니다.
     * DEFAULT 금고는 사용자당 1개만 생성할 수 있습니다.
     * WISHLIST 금고는 생성 시 목표 이름, 금액, 날짜를 설정할 수 있습니다.
     * 
     */
    public CreateVaultResponse createVault(CreateVaultRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        VaultKind vaultKind = parseVaultKind(request.vaultKind());

        if (vaultKind == VaultKind.DEFAULT
                && vaultRepository.existsByUserIdAndVaultKind(userId, VaultKind.DEFAULT)) {
            throw new CustomException(ErrorCode.VAULT_ALREADY_EXISTS);
        }

        Vault vault = Vault.builder()
                .userId(userId)
                .vaultKind(vaultKind)
                .balance(0L)
                .status(VaultStatus.LOCKED)
                .build();

        if (vaultKind == VaultKind.WISHLIST && request.wishlistName() != null) {
            vault.setWishlistGoal(request.wishlistName(), request.goalAmount(), request.goalDate());
        }

        Vault saved = vaultRepository.save(vault);

        return CreateVaultResponse.builder()
                .vaultId(saved.getVaultId())
                .vaultKind(saved.getVaultKind().name())
                .balance(saved.getBalance())
                .status(saved.getStatus().name())
                .build();
    }

    /**
     * 사용자의 주 계좌에서 돈을 출금하여 특정 금고에 입금합니다.
     * 외부 금융 API(SSAFY FinAPI)와 연동되어 실제 주 계좌에서 출금이 발생합니다.
     *
     * @param vaultId 입금할 대상 금고 ID
     * @param request 입금 금액 정보
     * @return 입금 처리 후의 금고 상태 응답
     * @throws CustomException 주 계좌 잔액 부족 또는 외부 API 통신 실패 시
     */
    public VaultDepositResponse deposit(Long vaultId, VaultDepositRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Vault vault = getActiveVault(vaultId, userId);
        String mainAccountNo = getMainAccount(userId).getAccountNumber();

        try {
            bankingClient.withdrawal(
                    userId,
                    new AccountWithdrawalCommand(
                            mainAccountNo,
                            String.valueOf(request.amount()),
                            "금고 입금"));
        } catch (CustomException e) {
            log.warn("금고 입금을 위한 주 계좌 출금 실패 vaultId={} accountNo={} message={}",
                    vaultId, mainAccountNo, e.getMessage());
            throw e;
        }

        vault.deposit(request.amount());

        return VaultDepositResponse.builder()
                .vaultId(vault.getVaultId())
                .depositedAmount(request.amount())
                .balanceAfter(vault.getBalance())
                .build();
    }

    /**
     * 특정 금고에서 돈을 긴급 출금하여 사용자의 주 계좌로 입금합니다.
     * 금고의 잔액이 차감된 후 외부 금융 API를 통해 주 계좌 입금이 이루어집니다.
     *
     * @param vaultId 출금할 대상 금고 ID
     * @param request 출금 금액 정보
     * @return 출금 처리 후의 금고 상태 응답
     * @throws CustomException 금고 잔액 부족 또는 외부 API 통신 실패 시
     */
    public VaultWithdrawResponse withdraw(Long vaultId, VaultWithdrawRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Vault vault = getActiveVault(vaultId, userId);
        String mainAccountNo = getMainAccount(userId).getAccountNumber();

        vault.withdraw(request.amount());

        try {
            bankingClient.deposit(
                    userId,
                    new AccountDepositCommand(
                            mainAccountNo,
                            String.valueOf(request.amount()),
                            "금고 긴급 출금"));
        } catch (CustomException e) {
            log.warn("금고 출금 후 주 계좌 입금 실패 vaultId={} accountNo={} message={}",
                    vaultId, mainAccountNo, e.getMessage());
            throw e;
        }

        return VaultWithdrawResponse.builder()
                .vaultId(vault.getVaultId())
                .withdrawnAmount(request.amount())
                .balanceAfter(vault.getBalance())
                .build();
    }

    /**
     * WISHLIST 금고의 위시리스트 목표 이름, 금액, 날짜를 설정하거나 수정합니다.
     *
     * @param vaultId 대상 금고 ID
     * @param request 설정할 위시리스트 목표 정보
     * @return 설정 반영 후의 금고 요약 정보
     */
    public VaultSummaryResponse setWishlistGoal(Long vaultId, SetWishlistGoalRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        Vault vault = getActiveVault(vaultId, userId);
        vault.setWishlistGoal(request.wishlistName(), request.goalAmount(), request.goalDate());
        return toVaultSummaryResponse(vault);
    }

    public VaultSummaryResponse achieveWishlist(Long vaultId) {
        Long userId = currentUserProvider.getCurrentUserId();
        Vault vault = getActiveVault(vaultId, userId);
        vault.achieve();
        return toVaultSummaryResponse(vault);
    }

    private Vault getActiveVault(Long vaultId, Long userId) {
        Vault vault = vaultRepository.findByVaultIdAndUserId(vaultId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.VAULT_NOT_FOUND));

        if (vault.getStatus() == VaultStatus.CLOSED) {
            throw new CustomException(ErrorCode.VAULT_ALREADY_ACHIEVED);
        }

        return vault;
    }

    private Account getMainAccount(Long userId) {
        List<Account> accounts = accountRepository.findAllByUserIdAndStatusAndIsDeletedFalse(userId,
                AccountStatus.ACTIVE);
        if (accounts.isEmpty()) {
            throw new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        return accounts.get(0);
    }

    private VaultKind parseVaultKind(String vaultKind) {
        try {
            return VaultKind.valueOf(vaultKind.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "유효하지 않은 금고 종류입니다. (DEFAULT 또는 WISHLIST)");
        }
    }

    private VaultSummaryResponse toVaultSummaryResponse(Vault vault) {
        return VaultSummaryResponse.builder()
                .vaultId(vault.getVaultId())
                .vaultKind(vault.getVaultKind().name())
                .balance(vault.getBalance())
                .status(vault.getStatus().name())
                .wishlistName(vault.getWishlistName())
                .goalAmount(vault.getGoalAmount())
                .goalDate(vault.getGoalDate())
                .isAchieved(vault.getIsAchieved())
                .build();
    }
}
