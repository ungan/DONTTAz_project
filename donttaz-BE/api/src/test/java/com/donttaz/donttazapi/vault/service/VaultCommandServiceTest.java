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

        @InjectMocks
        private VaultCommandService vaultCommandService;

        private static final Long USER_ID = 1L;
        private static final Long VAULT_ID = 10L;

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

                VaultDepositResponse response = vaultCommandService.deposit(VAULT_ID, VaultKind.WISHLIST,
                                new com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest("1000000000",
                                                100000L));

                assertThat(response.depositedAmount()).isEqualTo(100000L);
                assertThat(response.lockedAmount()).isEqualTo(100000L);
        }
}
