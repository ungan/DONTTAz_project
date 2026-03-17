package com.donttaz.donttazapi.vault.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import com.donttaz.donttazapi.vault.repository.VaultRepository;
import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VaultCommandServiceTest {

        @Mock
        private VaultRepository vaultRepository;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private BankingClient bankingClient;

        @Mock
        private CurrentUserProvider currentUserProvider;

        @InjectMocks
        private VaultCommandService vaultCommandService;

        private static final Long USER_ID = 1L;
        private static final Long VAULT_ID = 10L;

        @Test
        @DisplayName("DEFAULT 금고를 처음 생성하면 성공한다.")
        void createDefaultVault_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);
                given(vaultRepository.existsByUserIdAndVaultKind(USER_ID, VaultKind.DEFAULT)).willReturn(false);

                Vault saved = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.DEFAULT)
                                .balance(0L)
                                .status(VaultStatus.LOCKED)
                                .build();
                given(vaultRepository.save(any(Vault.class))).willReturn(saved);

                CreateVaultRequest request = new CreateVaultRequest("DEFAULT", null, null, null);
                CreateVaultResponse response = vaultCommandService.createVault(request);

                assertThat(response.vaultKind()).isEqualTo("DEFAULT");
                assertThat(response.balance()).isEqualTo(0L);
                assertThat(response.status()).isEqualTo("LOCKED");
                verify(vaultRepository).save(any(Vault.class));
        }

        @Test
        @DisplayName("DEFAULT 금고가 이미 존재하면 VAULT_ALREADY_EXISTS 예외가 발생한다.")
        void createDefaultVault_alreadyExists_throwsException() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);
                given(vaultRepository.existsByUserIdAndVaultKind(USER_ID, VaultKind.DEFAULT)).willReturn(true);

                CreateVaultRequest request = new CreateVaultRequest("DEFAULT", null, null, null);

                assertThatThrownBy(() -> vaultCommandService.createVault(request))
                                .isInstanceOf(CustomException.class)
                                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                                                .isEqualTo(ErrorCode.VAULT_ALREADY_EXISTS));
        }

        @Test
        @DisplayName("WISHLIST 금고 생성 시 목표 정보가 함께 설정된다.")
        void createWishlistVault_withGoal_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault saved = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.WISHLIST)
                                .balance(0L)
                                .status(VaultStatus.LOCKED)
                                .wishlistName("제주도 여행")
                                .goalAmount(500000L)
                                .build();
                given(vaultRepository.save(any(Vault.class))).willReturn(saved);

                CreateVaultRequest request = new CreateVaultRequest("WISHLIST", "제주도 여행", 500000L, null);
                CreateVaultResponse response = vaultCommandService.createVault(request);

                assertThat(response.vaultKind()).isEqualTo("WISHLIST");
        }

        @Test
        @DisplayName("금고 입금 시 주 계좌에서 출금하고 금고 잔액이 증가한다.")
        void deposit_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault vault = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.DEFAULT)
                                .balance(0L)
                                .status(VaultStatus.LOCKED)
                                .build();
                given(vaultRepository.findByVaultIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                Account account = Account.builder()
                                .userId(USER_ID)
                                .bankCode("001")
                                .bankName("한국은행")
                                .accountNumber("001-001-12345678")
                                .balance(5000000L)
                                .status(AccountStatus.ACTIVE)
                                .build();
                given(accountRepository.findAllByUserIdAndStatusAndIsDeletedFalse(USER_ID, AccountStatus.ACTIVE))
                                .willReturn(List.of(account));

                VaultDepositRequest request = new VaultDepositRequest(100000L);
                VaultDepositResponse response = vaultCommandService.deposit(VAULT_ID, request);

                assertThat(response.depositedAmount()).isEqualTo(100000L);
                assertThat(response.balanceAfter()).isEqualTo(100000L);
                verify(bankingClient).withdrawal(any(), any());
        }

        @Test
        @DisplayName("금고 잔액이 부족하면 VAULT_INSUFFICIENT_BALANCE 예외가 발생한다.")
        void withdraw_insufficientBalance_throwsException() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault vault = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.DEFAULT)
                                .balance(50000L)
                                .status(VaultStatus.LOCKED)
                                .build();
                given(vaultRepository.findByVaultIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                Account account = Account.builder()
                                .userId(USER_ID)
                                .bankCode("001")
                                .bankName("한국은행")
                                .accountNumber("001-001-12345678")
                                .balance(5000000L)
                                .status(AccountStatus.ACTIVE)
                                .build();
                given(accountRepository.findAllByUserIdAndStatusAndIsDeletedFalse(USER_ID, AccountStatus.ACTIVE))
                                .willReturn(List.of(account));

                VaultWithdrawRequest request = new VaultWithdrawRequest(100000L);

                assertThatThrownBy(() -> vaultCommandService.withdraw(VAULT_ID, request))
                                .isInstanceOf(CustomException.class)
                                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                                                .isEqualTo(ErrorCode.VAULT_INSUFFICIENT_BALANCE));
        }

        @Test
        @DisplayName("WISHLIST 금고 목표 달성 시 isAchieved = true, status = CLOSED 로 변경된다.")
        void achieveWishlist_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault vault = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.WISHLIST)
                                .balance(500000L)
                                .status(VaultStatus.LOCKED)
                                .wishlistName("제주도 여행")
                                .goalAmount(300000L)
                                .isAchieved(false)
                                .build();
                given(vaultRepository.findByVaultIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                VaultSummaryResponse response = vaultCommandService.achieveWishlist(VAULT_ID);

                assertThat(response.isAchieved()).isTrue();
                assertThat(response.status()).isEqualTo("CLOSED");
        }

        @Test
        @DisplayName("잔액이 목표 금액 미달이면 VAULT_GOAL_NOT_MET 예외가 발생한다.")
        void achieveWishlist_goalNotMet_throwsException() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault vault = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.WISHLIST)
                                .balance(100000L)
                                .status(VaultStatus.LOCKED)
                                .wishlistName("제주도 여행")
                                .goalAmount(500000L)
                                .isAchieved(false)
                                .build();
                given(vaultRepository.findByVaultIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                assertThatThrownBy(() -> vaultCommandService.achieveWishlist(VAULT_ID))
                                .isInstanceOf(CustomException.class)
                                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                                                .isEqualTo(ErrorCode.VAULT_GOAL_NOT_MET));
        }

        @Test
        @DisplayName("WISHLIST 금고 위시리스트 목표를 설정하면 응답에 반영된다.")
        void setWishlistGoal_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault vault = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.WISHLIST)
                                .balance(0L)
                                .status(VaultStatus.LOCKED)
                                .isAchieved(false)
                                .build();
                given(vaultRepository.findByVaultIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                SetWishlistGoalRequest request = new SetWishlistGoalRequest("노트북 구매", 1500000L, null);
                VaultSummaryResponse response = vaultCommandService.setWishlistGoal(VAULT_ID, request);

                assertThat(response.wishlistName()).isEqualTo("노트북 구매");
                assertThat(response.goalAmount()).isEqualTo(1500000L);
        }
}
