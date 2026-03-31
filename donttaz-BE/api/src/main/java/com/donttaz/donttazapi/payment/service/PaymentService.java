package com.donttaz.donttazapi.payment.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.response.AccountTransferItemResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.payment.domain.Merchant;
import com.donttaz.donttazapi.payment.domain.Payment;
import com.donttaz.donttazapi.payment.domain.PaymentStatus;
import com.donttaz.donttazapi.payment.dto.request.CreatePaymentRequest;
import com.donttaz.donttazapi.payment.dto.response.PaymentResponse;
import com.donttaz.donttazapi.payment.repository.MerchantRepository;
import com.donttaz.donttazapi.payment.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private static final String DEPOSIT_TRANSFER_SUMMARY = "PAYMENT_SETTLEMENT";

    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final AccountTransferService accountTransferService;

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Merchant merchant = merchantRepository.findByMerchantIdAndActiveTrue(request.merchantId())
                .orElseThrow(() -> new CustomException(ErrorCode.MERCHANT_NOT_FOUND));
        Account withdrawalAccount = accountRepository
                .findFirstByAccountNumberAndStatusAndIsDeletedFalse(request.withdrawalAccountNo(), AccountStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (merchant.getSettlementAccountNo().equals(request.withdrawalAccountNo())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "payment account and settlement account must be different.");
        }

        Long userId = withdrawalAccount.getUserId();
        AccountTransferResponse transferResponse = accountTransferService.transfer(
                userId,
                new AccountTransferRequest(
                        merchant.getSettlementAccountNo(),
                        DEPOSIT_TRANSFER_SUMMARY,
                        request.amount(),
                        request.withdrawalAccountNo(),
                        merchant.getName()
                )
        );

        AccountTransferItemResponse withdrawalTransaction = findTransaction(transferResponse, request.withdrawalAccountNo());
        AccountTransferItemResponse depositTransaction = findTransaction(transferResponse, merchant.getSettlementAccountNo());

        Payment payment = paymentRepository.save(
                Payment.builder()
                        .userId(userId)
                        .merchant(merchant)
                        .merchantName(merchant.getName())
                        .withdrawalAccountNo(request.withdrawalAccountNo())
                        .settlementAccountNo(merchant.getSettlementAccountNo())
                        .withdrawalTransactionId(withdrawalTransaction.transactionUniqueNo())
                        .depositTransactionId(depositTransaction.transactionUniqueNo())
                        .amount(parseAmount(request.amount()))
                        .status(PaymentStatus.COMPLETED)
                        .paidAt(parseTransactionDate(withdrawalTransaction.transactionDate()))
                        .build()
        );

        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .merchantId(merchant.getMerchantId())
                .merchantName(merchant.getName())
                .withdrawalAccountNo(payment.getWithdrawalAccountNo())
                .settlementAccountNo(payment.getSettlementAccountNo())
                .amount(payment.getAmount())
                .withdrawalTransactionId(payment.getWithdrawalTransactionId())
                .depositTransactionId(payment.getDepositTransactionId())
                .transactionDate(withdrawalTransaction.transactionDate())
                .status(payment.getStatus().name())
                .build();
    }

    private AccountTransferItemResponse findTransaction(AccountTransferResponse response, String accountNo) {
        return response.transactions().stream()
                .filter(transaction -> accountNo.equals(transaction.accountNo()))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "payment transaction result is missing."));
    }

    private long parseAmount(String amount) {
        try {
            return Long.parseLong(amount);
        } catch (NumberFormatException exception) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "payment amount is invalid.");
        }
    }

    private LocalDateTime parseTransactionDate(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            LocalDate date = LocalDate.parse(transactionDate, DateTimeFormatter.BASIC_ISO_DATE);
            return LocalDateTime.of(date, LocalTime.now());
        } catch (DateTimeParseException exception) {
            return LocalDateTime.now();
        }
    }
}