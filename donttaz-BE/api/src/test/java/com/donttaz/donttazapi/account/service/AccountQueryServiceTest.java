package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.response.AccountListResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionDetailResponse;
import com.donttaz.donttazapi.account.dto.response.TransactionListResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
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

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private DemandDepositService demandDepositService;

    @Mock
    private AccountSyncService accountSyncService;

    @Mock
    private BankingAccountQueryService bankingAccountQueryService;

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
    @DisplayName("getTransactions overlays merchant data for payment transactions")
    void getTransactionsSuccess() {
        Account account = Account.builder()
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
        TransactionQueryResult result = new TransactionQueryResult(
                3L,
                1,
                true,
                List.of(
                        new TransactionSummaryResult(
                                "61",
                                LocalDateTime.of(2024, 4, 1, 10, 32, 29),
                                "WITHDRAWAL",
                                10_000,
                                89_900_000,
                                "TRANSFER",
                                "WITHDRAWAL_TRANSFER"
                        )
                )
        );
        Payment payment = createPayment("61", "EMART", "0204667768182760");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(10L, 1L, AccountStatus.ACTIVE))
                .willReturn(Optional.of(account));
        given(bankingAccountQueryService.getTransactions("0016174648358792", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "A", 0, 20))
                .willReturn(result);
        given(paymentRepository.findAllByUserIdAndWithdrawalTransactionIdIn(1L, List.of("61")))
                .willReturn(List.of(payment));

        TransactionListResponse response = accountQueryService.getTransactions(
                10L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31),
                "ALL",
                0,
                20
        );

        assertThat(response.totalElements()).isEqualTo(3L);
        assertThat(response.transactions()).hasSize(1);
        assertThat(response.transactions().get(0).transactionId()).isEqualTo("61");
        assertThat(response.transactions().get(0).type()).isEqualTo("WITHDRAWAL");
        assertThat(response.transactions().get(0).counterpartyName()).isEqualTo("EMART");
        assertThat(response.transactions().get(0).category()).isEqualTo("PAYMENT");
    }

    @Test
    @DisplayName("getTransactionDetail overlays merchant data for payment transactions")
    void getTransactionDetailSuccess() {
        Account account = Account.builder()
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
        TransactionDetailResult result = new TransactionDetailResult(
                "61",
                "0016174648358792",
                LocalDateTime.of(2024, 4, 1, 10, 32, 29),
                "WITHDRAWAL",
                10_000,
                89_900_000,
                "TRANSFER",
                null,
                "0204667768182760",
                0,
                "WITHDRAWAL_TRANSFER",
                null
        );
        Payment payment = createPayment("61", "EMART", "0204667768182760");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByAccountIdAndUserIdAndStatusAndIsDeletedFalse(10L, 1L, AccountStatus.ACTIVE))
                .willReturn(Optional.of(account));
        given(bankingAccountQueryService.getTransactionDetail("0016174648358792", "61"))
                .willReturn(result);
        given(paymentRepository.findByUserIdAndWithdrawalTransactionId(1L, "61"))
                .willReturn(Optional.of(payment));

        TransactionDetailResponse response = accountQueryService.getTransactionDetail(10L, "61");

        assertThat(response.transactionId()).isEqualTo("61");
        assertThat(response.accountId()).isEqualTo(10L);
        assertThat(response.counterpartyName()).isEqualTo("EMART");
        assertThat(response.counterpartyAccount()).isEqualTo("0204667768182760");
        assertThat(response.category()).isEqualTo("PAYMENT");
        assertThat(response.memo()).isEqualTo("EMART");
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
