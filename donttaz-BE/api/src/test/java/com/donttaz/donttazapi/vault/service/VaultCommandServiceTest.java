package com.donttaz.donttazapi.vault.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.dto.request.CreateVaultRequest;
import com.donttaz.donttazapi.vault.dto.response.CreateVaultResponse;
import com.donttaz.donttazapi.vault.dto.response.VaultDepositResponse;
import com.donttaz.donttazapi.vault.repository.VaultRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import java.time.LocalDate;
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
        private com.donttaz.donttazapi.vault.repository.VaultHistoryRepository vaultHistoryRepository;

        @Mock
        private CurrentUserProvider currentUserProvider;

        @Mock
        private com.donttaz.donttazapi.account.service.AccountTransferService accountTransferService;

        @InjectMocks
        private VaultCommandService vaultCommandService;

        private static final Long USER_ID = 1L;
        private static final Long VAULT_ID = 10L;

        @Test
        @DisplayName("금고를 성공적으로 생성한다.")
        void createVault_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);
                given(vaultRepository.findAllByUserIdAndStatus(USER_ID, VaultStatus.LOCKED))
                                .willReturn(java.util.Collections.emptyList());

                Vault saved = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.WISHLIST)
                                .wishName("애플워치")
                                .goalAmount(500000L)
                                .targetDate(LocalDate.now().plusDays(30))
                                .lockedAmount(0L)

                                .build();

                given(vaultRepository.save(any(Vault.class))).willReturn(saved);

                CreateVaultRequest request = new CreateVaultRequest(VaultKind.WISHLIST, "애플워치", 500000L,
                                LocalDate.now().plusDays(30));
                CreateVaultResponse response = vaultCommandService.createVault(request);

                assertThat(response.lockedAmount()).isEqualTo(0L);
                assertThat(response.status()).isEqualTo("LOCKED");
                verify(vaultRepository).save(any(Vault.class));
        }

        @Test
        @DisplayName("금고 입금 시 lockedAmount가 증가한다.")
        void deposit_success() {
                given(currentUserProvider.getCurrentUserId()).willReturn(USER_ID);

                Vault vault = Vault.builder()
                                .userId(USER_ID)
                                .vaultKind(VaultKind.WISHLIST)
                                .wishName("애플워치")
                                .goalAmount(500000L)
                                .targetDate(LocalDate.now().plusDays(30))
                                .lockedAmount(0L)
                                .status(VaultStatus.LOCKED)
                                .build();
                given(vaultRepository.findByVaultIdAndUserId(VAULT_ID, USER_ID)).willReturn(Optional.of(vault));

                VaultDepositResponse response = vaultCommandService.deposit(VAULT_ID,
                                new com.donttaz.donttazapi.vault.dto.request.VaultDepositRequest("1000000000",
                                                100000L));

                assertThat(response.depositedAmount()).isEqualTo(100000L);
                assertThat(response.lockedAmount()).isEqualTo(100000L);
        }
}
