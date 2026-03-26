package com.donttaz.donttazapi.vault.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.memorandum.service.MemorandumService;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.request.CreateWishlistVaultRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.repository.DefaultVaultRepository;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VaultCommandServiceTest {

        @Mock
        private DefaultVaultRepository defaultVaultRepository;

        @Mock
        private WishlistVaultRepository wishlistVaultRepository;

        @Mock
        private VaultHistoryRepository vaultHistoryRepository;

        @Mock
        private CurrentUserProvider currentUserProvider;

        @Mock
        private BankingClient bankingClient;

        @Mock
        private MemorandumService memorandumService;

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

        private VaultCommandService vaultCommandService;

        private static final Long USER_ID = 1L;
        private static final Long VAULT_ID = 10L;

        @org.junit.jupiter.api.BeforeEach
        void setUp() {
                org.mockito.MockitoAnnotations.openMocks(this);
                vaultCommandService = new VaultCommandService(
                                defaultVaultRepository,
                                wishlistVaultRepository,
                                vaultHistoryRepository,
                                bankingClient,
                                currentUserProvider,
                                memorandumService,
                                rabbitTemplate,
                                accountRepository);

                org.springframework.test.util.ReflectionTestUtils.setField(vaultCommandService, "adminUserId", 1L);
                org.springframework.test.util.ReflectionTestUtils.setField(vaultCommandService, "adminAccountNo",
                                "9993479974590214");
        }

        @Test
        @DisplayName("위시리스트 금고를 성공적으로 생성한다.")
        void createVault_wishlist_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);
                given(wishlistVaultRepository.existsByUserIdAndStatus(USER_ID, VaultStatus.LOCKED)).willReturn(false);

                WishlistVault saved = WishlistVault.builder()
                                .userId(USER_ID)
                                .wishName("애플워치")
                                .goalAmount(500000L)
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();

                given(wishlistVaultRepository.save(any(WishlistVault.class))).willReturn(saved);
                given(memorandumService.getMemorandumByVaultId(any())).willReturn(
                                com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse.builder()
                                                .build());

                CreateWishlistVaultRequest request = new CreateWishlistVaultRequest("애플워치", 500000L);
                CreateVaultResponse response = vaultCommandService.createWishlistVault(request);

                assertThat(response.lockedAmount()).isEqualTo(0L);
                assertThat(response.status()).isEqualTo("LOCKED");
                verify(wishlistVaultRepository).save(any(WishlistVault.class));
        }

        @Test
        @DisplayName("위시리스트 금고 입금 시 lockedAmount가 증가한다.")
        void deposit_wishlist_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                WishlistVault vault = WishlistVault.builder()
                                .userId(USER_ID)
                                .wishName("애플워치")
                                .goalAmount(500000L)
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();
                given(wishlistVaultRepository.findByIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));
                given(accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(any(), any()))
                                .willReturn(Optional.of(Account.builder()
                                                .accountNumber("1000000000")
                                                .status(AccountStatus.ACTIVE)
                                                .build()));

                VaultDepositResponse response = vaultCommandService.deposit(VAULT_ID, VaultKind.WISHLIST,
                                new com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest(100000L));

                assertThat(response.depositedAmount()).isEqualTo(100000L);
                assertThat(response.lockedAmount()).isEqualTo(100000L);
        }

        @Test
        @DisplayName("위시리스트 금고 출금 시 패널티 이벤트가 발행된다.")
        void withdraw_wishlist_publishesEvent() {
                // Given
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                WishlistVault vault = WishlistVault.builder()
                                .userId(USER_ID)
                                .wishName("애플워치")
                                .goalAmount(500000L)
                                .lockedAmount(100000L)
                                .status(VaultStatus.LOCKED)
                                .build();
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "id", VAULT_ID);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "penaltyInterestRate", 10.0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "baseInterestRate", 12.0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "withdrawalCount", 0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "createdAt",
                                java.time.LocalDateTime.now().minusDays(10));
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "lastWithdrawalAt",
                                java.time.LocalDateTime.now());
                given(wishlistVaultRepository.findByIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                // When
                com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest request = new com.donttaz.donttazapi.vault.dto.request.VaultWithdrawRequest(
                                "1000000000", 50000L);
                vaultCommandService.withdraw(VAULT_ID, VaultKind.WISHLIST, request);

                // Then
                verify(rabbitTemplate).convertAndSend(
                                org.mockito.ArgumentMatchers.eq(
                                                com.donttaz.donttazapi.common.config.RabbitMQConfig.PENALTY_EXCHANGE),
                                org.mockito.ArgumentMatchers.eq(
                                                com.donttaz.donttazapi.common.config.RabbitMQConfig.PENALTY_ROUTING_KEY),
                                org.mockito.ArgumentMatchers.<com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent>argThat(
                                                penaltyEvent -> penaltyEvent.wishlistVaultId().equals(VAULT_ID) &&
                                                                penaltyEvent.withdrawAmount().equals(50000L) &&
                                                                !penaltyEvent.terminated() &&
                                                                !penaltyEvent.success()));
        }

        @Test
        @DisplayName("위시리스트 금고 중도 해지 시 전체 잔액이 인출되고 해지 이벤트가 발행된다.")
        void terminate_wishlist_publishesEvent_and_setsStatusToBroken() {
                // Given
                WishlistVault vault = WishlistVault.builder()
                                .userId(USER_ID)
                                .wishName("아이패드")
                                .goalAmount(1000000L)
                                .lockedAmount(200000L)
                                .status(VaultStatus.LOCKED)
                                .build();
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "id", VAULT_ID);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "penaltyInterestRate", 10.0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "baseInterestRate", 12.0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "withdrawalCount", 0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "createdAt",
                                java.time.LocalDateTime.now().minusDays(10));
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "lastWithdrawalAt",
                                java.time.LocalDateTime.now());

                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);
                given(wishlistVaultRepository.findByIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));
                given(accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(any(), any()))
                                .willReturn(Optional.of(Account.builder()
                                                .accountNumber("1000000000")
                                                .status(AccountStatus.ACTIVE)
                                                .build()));

                // When
                vaultCommandService.terminateWishlist(VAULT_ID);

                // Then
                org.assertj.core.api.Assertions.assertThat(vault.getStatus()).isEqualTo(VaultStatus.BROKEN);
                org.assertj.core.api.Assertions.assertThat(vault.getLockedAmount()).isEqualTo(0L);

                verify(rabbitTemplate).convertAndSend(
                                org.mockito.ArgumentMatchers.eq(
                                                com.donttaz.donttazapi.common.config.RabbitMQConfig.PENALTY_EXCHANGE),
                                org.mockito.ArgumentMatchers.eq(
                                                com.donttaz.donttazapi.common.config.RabbitMQConfig.PENALTY_ROUTING_KEY),
                                org.mockito.ArgumentMatchers.<com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent>argThat(
                                                penaltyEvent -> penaltyEvent.wishlistVaultId().equals(VAULT_ID) &&
                                                                penaltyEvent.withdrawAmount().equals(200000L) &&
                                                                penaltyEvent.terminated() &&
                                                                !penaltyEvent.success()));
        }

        @Test
        @DisplayName("위시리스트 금고 목표 달성 후 해지 시 정상 해지되고 축하 이벤트가 발행된다.")
        void terminate_wishlist_success_publishesCongratsEvent() {
                // Given
                WishlistVault vault = WishlistVault.builder()
                                .userId(USER_ID)
                                .wishName("아이패드")
                                .goalAmount(200000L) // 목표 금액
                                .lockedAmount(200000L) // 달성 상태
                                .status(VaultStatus.LOCKED)
                                .build();
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "id", VAULT_ID);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "penaltyInterestRate", 10.0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "baseInterestRate", 12.0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "withdrawalCount", 0);
                org.springframework.test.util.ReflectionTestUtils.setField(vault, "createdAt",
                                java.time.LocalDateTime.now().minusDays(10));

                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);
                given(wishlistVaultRepository.findByIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));
                given(accountRepository.findByUserIdAndIsPrimaryTrueAndStatusAndIsDeletedFalse(any(), any()))
                                .willReturn(Optional.of(Account.builder()
                                                .accountNumber("1000000000")
                                                .status(AccountStatus.ACTIVE)
                                                .build()));

                // When
                vaultCommandService.terminateWishlist(VAULT_ID);

                // Then
                org.assertj.core.api.Assertions.assertThat(vault.getStatus()).isEqualTo(VaultStatus.MATURED);
                org.assertj.core.api.Assertions.assertThat(vault.getLockedAmount()).isEqualTo(0L);

                verify(rabbitTemplate).convertAndSend(
                                org.mockito.ArgumentMatchers.eq(
                                                com.donttaz.donttazapi.common.config.RabbitMQConfig.PENALTY_EXCHANGE),
                                org.mockito.ArgumentMatchers.eq(
                                                com.donttaz.donttazapi.common.config.RabbitMQConfig.PENALTY_ROUTING_KEY),
                                org.mockito.ArgumentMatchers.<com.donttaz.donttazapi.webhook.event.VaultPenaltyEvent>argThat(
                                                penaltyEvent -> penaltyEvent.wishlistVaultId().equals(VAULT_ID) &&
                                                                penaltyEvent.withdrawAmount().equals(202000L) &&
                                                                penaltyEvent.terminated() &&
                                                                penaltyEvent.success()));
        }
}
