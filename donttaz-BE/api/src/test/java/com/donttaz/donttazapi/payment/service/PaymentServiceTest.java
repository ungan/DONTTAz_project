package com.donttaz.donttazapi.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.response.AccountTransferItemResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import com.donttaz.donttazapi.payment.domain.Merchant;
import com.donttaz.donttazapi.payment.domain.Payment;
import com.donttaz.donttazapi.payment.dto.request.CreatePaymentRequest;
import com.donttaz.donttazapi.payment.dto.response.PaymentResponse;
import com.donttaz.donttazapi.payment.repository.MerchantRepository;
import com.donttaz.donttazapi.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountTransferService accountTransferService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("createPayment resolves account owner and sends merchant name in withdrawal summary")
    void createPaymentSuccess() {
        Merchant merchant = Merchant.builder()
                .name("EMART")
                .settlementAccountNo("0204667768182760")
                .category("MART")
                .active(Boolean.TRUE)
                .build();
        ReflectionTestUtils.setField(merchant, "merchantId", 1L);

        Account withdrawalAccount = Account.builder()
                .userId(7L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("0016174648358792")
                .balance(500_000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();

        CreatePaymentRequest request = new CreatePaymentRequest("0016174648358792", 1L, "10000");
        AccountTransferResponse transferResponse = AccountTransferResponse.builder()
                .transactions(List.of(
                        AccountTransferItemResponse.builder()
                                .transactionUniqueNo("61")
                                .accountNo("0016174648358792")
                                .transactionDate("20240401")
                                .transactionType("2")
                                .transactionTypeName("WITHDRAWAL_TRANSFER")
                                .transactionAccountNo("0204667768182760")
                                .build(),
                        AccountTransferItemResponse.builder()
                                .transactionUniqueNo("62")
                                .accountNo("0204667768182760")
                                .transactionDate("20240401")
                                .transactionType("1")
                                .transactionTypeName("DEPOSIT_TRANSFER")
                                .transactionAccountNo("0016174648358792")
                                .build()
                ))
                .build();

        given(merchantRepository.findByMerchantIdAndActiveTrue(1L)).willReturn(Optional.of(merchant));
        given(accountRepository.findFirstByAccountNumberAndStatusAndIsDeletedFalse("0016174648358792", AccountStatus.ACTIVE))
                .willReturn(Optional.of(withdrawalAccount));
        given(accountTransferService.transfer(any(Long.class), any(AccountTransferRequest.class))).willReturn(transferResponse);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            ReflectionTestUtils.setField(payment, "paymentId", 10L);
            return payment;
        });

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.paymentId()).isEqualTo(10L);
        assertThat(response.merchantId()).isEqualTo(1L);
        assertThat(response.merchantName()).isEqualTo("EMART");
        assertThat(response.amount()).isEqualTo(10_000L);
        assertThat(response.withdrawalTransactionId()).isEqualTo("61");
        assertThat(response.depositTransactionId()).isEqualTo("62");
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.transactionDate()).isEqualTo("20240401");

        verify(accountTransferService).transfer(argThat(userId -> userId.equals(7L)), argThat(transferRequest ->
                transferRequest.depositAccountNo().equals("0204667768182760")
                        && transferRequest.depositTransactionSummary().equals("PAYMENT_SETTLEMENT")
                        && transferRequest.transactionBalance().equals("10000")
                        && transferRequest.withdrawalAccountNo().equals("0016174648358792")
                        && transferRequest.withdrawalTransactionSummary().equals("EMART")
        ));
    }
}