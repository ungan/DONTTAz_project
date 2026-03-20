package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.vault.domain.TransactionType;
import com.donttaz.donttazapi.vault.domain.DefaultVault;
import com.donttaz.donttazapi.vault.domain.WishlistVault;
import com.donttaz.donttazapi.vault.domain.VaultKind;
import com.donttaz.donttazapi.vault.domain.VaultHistory;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.vault.repository.DefaultVaultRepository;
import com.donttaz.donttazapi.vault.repository.WishlistVaultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaultInterestBatchService {

    private final DefaultVaultRepository defaultVaultRepository;
    private final WishlistVaultRepository wishlistVaultRepository;
    private final VaultHistoryRepository vaultHistoryRepository;

    /**
     * 매일 자정에 모든 활성 금고에 하루치 이자를 지급합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void executeDailyInterestBatch() {
        log.info("금고 일일 이자 지급 배치 시작");
        int count = 0;

        // 1. 기본 금고 처리
        List<DefaultVault> defaultVaults = defaultVaultRepository.findAllByStatus(VaultStatus.LOCKED);
        for (DefaultVault vault : defaultVaults) {
            Long dailyInterest = vault.calculateDailyInterest();
            if (dailyInterest > 0) {
                vault.addInterest(dailyInterest);
                saveInterestHistory(vault.getId(), VaultKind.DEFAULT, dailyInterest);
                count++;
            }
        }

        // 2. 위시리스트 금고 처리
        List<WishlistVault> wishlistVaults = wishlistVaultRepository.findAllByStatus(VaultStatus.LOCKED);
        for (WishlistVault vault : wishlistVaults) {
            Long dailyInterest = vault.calculateDailyInterest();
            if (dailyInterest > 0) {
                vault.addInterest(dailyInterest);
                saveInterestHistory(vault.getId(), VaultKind.WISHLIST, dailyInterest);
                count++;
            }
        }

        log.info("금고 일일 이자 지급 완료 (총 {}개 금고 처리됨)", count);
    }

    private void saveInterestHistory(Long vaultId, VaultKind kind, Long amount) {
        vaultHistoryRepository.save(VaultHistory.builder()
                .vaultId(vaultId)
                .vaultKind(kind)
                .transactionType(TransactionType.DEPOSIT)
                .amount(amount)
                .interestPaid(amount)
                .build());
    }
}
