package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.ai.repository.TransactionAiMetadataRepository;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.payment.domain.Merchant;
import com.donttaz.donttazapi.payment.domain.Payment;
import com.donttaz.donttazapi.payment.domain.PaymentStatus;
import com.donttaz.donttazapi.payment.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionAiMetadataRepository transactionAiMetadataRepository;

    @Mock
    private DemandDepositService demandDepositService;

    @Mock
    private AccountSyncService accountSyncService;

    @Mock
    private BankingAccountQueryService bankingAccountQueryService;

    @Mock
    private TransactionSyncService transactionSyncService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountQueryService accountQueryService;

    @Test
    @DisplayName("getAccounts returns balances and primary metadata")
    void getAccountsSuccess() {
        List<DemandDepositAccountResult> results = List.of(
                new DemandDepositAccountResult(
                        "020",
                        "WOORI",
                        "USER",
                        "0201238182760",
                        "Demand Deposit",
                        "1",
                        "DEPOSIT",
                        "20240320",
                        "20290320",
                        "100000000",
                        "20000000",
                        "8003477",
                        "20240323",
                        "KRW"
                )
        );
        given(demandDepositService.getAccounts()).willReturn(results);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndStatusAndIsDeletedFalse(1L, AccountStatus.ACTIVE))
                .willReturn(List.of(
                        Account.builder()
                                .userId(1L)
                                .bankCode("020")
                                .bankName("WOORI")
                                .accountNumber("0201238182760")
                                .balance(8_003_477L)
                                .dailyTransferLimit(100_000_000L)
                                .oneTimeTransferLimit(20_000_000L)
                                .linkedAt(LocalDateTime.now())
                                .lastSyncedAt(LocalDateTime.now())
                                .isDeleted(Boolean.FALSE)
                                .isPrimary(Boolean.TRUE)
                                .status(AccountStatus.ACTIVE)
                                .build()
                ));

        AccountListResponse response = accountQueryService.getAccounts();

        assertThat(response.totalBalance()).isEqualTo(8_003_477L);
        assertThat(response.accountList()).hasSize(1);
        assertThat(response.accountList().get(0).bankCode()).isEqualTo("020");
        assertThat(response.accountList().get(0).accountNo()).isEqualTo("0201238182760");
        assertThat(response.accountList().get(0).maskedAccountNo()).isEqualTo("020-***-182760");
        assertThat(response.accountList().get(0).isPrimary()).isTrue();
        verify(demandDepositService).getAccounts();
        verify(accountSyncService).syncDemandDepositAccounts(results);
    }

    @Test
    @DisplayName("getTransactions reads from DB and overlays merchant data for payment rows")
    void getTransactionsSuccess() {
        Account account = createAccount();
        ReflectionTestUtils.setField(account, "accountId", 10L);
        Transaction transaction = Transaction.builder()
                .account(account)
                .userId(1L)
                .source("FIN_API")
                .sourceTxId("61")
                .txType("WITHDRAWAL")
                .txTypeName("WITHDRAWAL_TRANSFER")
                .amount(10_000L)
                .balanceSnapshot(89_900_000L)
                .counterpartyName("TRANSFER")
                .transactionSummary("TRANSFER")
                .transactionAt(LocalDateTime.of(2024, 4, 1, 10, 32, 29))
                .syncedAt(LocalDateTime.of(2024, 4, 1, 10, 33, 0))
                .build();
        ReflectionTestUtils.setField(transaction, "id", 701L);
        Payment payment = createPayment("61", "EMART", "0204667768182760");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(10L, 1L, AccountStatus.ACTIVE))
                .willReturn(Optional.of(account));
        given(transactionSyncService.syncTransactions(account, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .willReturn(List.of());
        given(transactionRepository.search(eq(10L), eq(1L), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(transaction)));
        given(paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(1L, List.of("61")))
                .willReturn(List.of(payment));
        given(transactionAiMetadataRepository.findAllByTransactionIdIn(List.of(701L)))
                .willReturn(List.of());

        TransactionListResponse response = accountQueryService.getTransactions(
                10L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "ALL",
                0,
                20
        );

        assertThat(response.totalElements()).isEqualTo(1L);
        assertThat(response.transactions()).hasSize(1);
        assertThat(response.transactions().get(0).transactionId()).isEqualTo("61");
        assertThat(response.transactions().get(0).type()).isEqualTo("WITHDRAWAL");
        assertThat(response.transactions().get(0).counterpartyName()).isEqualTo("EMART");
        assertThat(response.transactions().get(0).category()).isEqualTo("PAYMENT");

        verify(transactionSyncService, times(1))
                .syncTransactions(account, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("getTransactionDetail reads stored transaction and overlays merchant data")
    void getTransactionDetailSuccess() {
        Account account = createAccount();
        ReflectionTestUtils.setField(account, "accountId", 10L);
        Transaction transaction = Transaction.builder()
                .account(account)
                .userId(1L)
                .source("FIN_API")
                .sourceTxId("61")
                .txType("WITHDRAWAL")
                .txTypeName("WITHDRAWAL_TRANSFER")
                .amount(10_000L)
                .balanceSnapshot(89_900_000L)
                .counterpartyName("TRANSFER")
                .counterpartyAccountNo("0204667768182760")
                .transactionSummary("TRANSFER")
                .transactionAt(LocalDateTime.of(2024, 4, 1, 10, 32, 29))
                .syncedAt(LocalDateTime.of(2024, 4, 1, 10, 33, 0))
                .build();
        ReflectionTestUtils.setField(transaction, "id", 701L);
        Payment payment = createPayment("61", "EMART", "0204667768182760");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(10L, 1L, AccountStatus.ACTIVE))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByIdAndAccountAccountIdAndAccountUserId(61L, 10L, 1L))
                .willReturn(Optional.empty());
        given(transactionRepository.findByAccountAccountIdAndAccountUserIdAndSourceTxId(10L, 1L, "61"))
                .willReturn(Optional.of(transaction));
        given(paymentRepository.findByUserIdAndWithdrawalTransactionId(1L, "61"))
                .willReturn(Optional.of(payment));
        given(transactionAiMetadataRepository.findByTransactionId(701L))
                .willReturn(Optional.empty());

        TransactionDetailResponse response = accountQueryService.getTransactionDetail(10L, "61");

        assertThat(response.transactionId()).isEqualTo("61");
        assertThat(response.accountId()).isEqualTo(10L);
        assertThat(response.counterpartyName()).isEqualTo("EMART");
        assertThat(response.counterpartyAccount()).isEqualTo("0204667768182760");
        assertThat(response.category()).isEqualTo("PAYMENT");
        assertThat(response.memo()).isEqualTo("EMART");
    }

    private Account createAccount() {
        return Account.builder()
                .userId(1L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("0016174648358792")
                .balance(89_900_000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private Payment createPayment(String withdrawalTransactionId, String merchantName, String settlementAccountNo) {
        Merchant merchant = Merchant.builder()
                .name(merchantName)
                .settlementAccountNo(settlementAccountNo)
                .category("MART")
                .active(Boolean.TRUE)
                .build();

        return Payment.builder()
                .userId(1L)
                .merchant(merchant)
                .merchantName(merchantName)
                .withdrawalAccountNo("0016174648358792")
                .settlementAccountNo(settlementAccountNo)
                .withdrawalTransactionId(withdrawalTransactionId)
                .depositTransactionId("62")
                .amount(10_000L)
                .status(PaymentStatus.COMPLETED)
                .paidAt(LocalDateTime.of(2024, 4, 1, 10, 32, 29))
                .build();
    }
}
