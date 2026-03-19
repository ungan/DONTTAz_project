package com.donttaz.donttazapi.vault.service;

import com.donttaz.donttazapi.vault.domain.TransactionType;
import com.donttaz.donttazapi.vault.domain.Vault;
import com.donttaz.donttazapi.vault.domain.VaultHistory;
import com.donttaz.donttazapi.vault.domain.VaultStatus;
import com.donttaz.donttazapi.vault.repository.VaultHistoryRepository;
import com.donttaz.donttazapi.vault.repository.VaultRepository;
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

    private final VaultRepository vaultRepository;
    private final VaultHistoryRepository vaultHistoryRepository;

    /**
     * 매일 자정에 모든 활성 금고에 하루치 이자를 지급합니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void executeDailyInterestBatch() {
        log.info("금고 일일 이자 지급 배치 시작");

        List<Vault> activeVaults = vaultRepository.findAllByStatus(VaultStatus.LOCKED);
        int count = 0;

        for (Vault vault : activeVaults) {
            Long dailyInterest = vault.calculateDailyInterest();

            if (dailyInterest > 0) {
                vault.addInterest(dailyInterest);

                vaultHistoryRepository.save(VaultHistory.builder()
                        .vaultId(vault.getVaultId())
                        .transactionType(TransactionType.DEPOSIT) // 또는 전용 타입 INTEREST 있으면 변경 가능
                        .amount(dailyInterest)
                        .interestPaid(dailyInterest)
                        .build());

                count++;
            }
        }

        log.info("금고 일일 이자 지급 완료 (총 {}개 금고 처리됨)", count);
    }
}
