package com.donttaz.donttazapi.infra.banking.mapper;

import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountSummaryResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDetailResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountItemResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiOneWonConfirmResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiOneWonRequestResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransactionDetailResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransactionItemResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransactionListResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiTransferResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FinApiBankingMapper {

    public List<AccountSummaryResult> toAccountSummaryResults(List<FinApiAccountItemResponse> responses) {
        return responses.stream()
                .map(this::toAccountSummaryResult)
                .toList();
    }

    public AccountSummaryResult toAccountSummaryResult(FinApiAccountItemResponse response) {
        return new AccountSummaryResult(
                response.accountId(),
                response.bankCode(),
                response.bankName(),
                response.accountNumber(),
                response.accountAlias(),
                response.balance()
        );
    }

    public AccountDetailResult toAccountDetailResult(FinApiAccountDetailResponse response) {
        return new AccountDetailResult(
                response.accountId(),
                response.bankCode(),
                response.bankName(),
                response.accountNumber(),
                response.accountAlias(),
                response.balance(),
                response.linkedAt()
        );
    }

    public TransactionQueryResult toTransactionQueryResult(FinApiTransactionListResponse response) {
        List<TransactionSummaryResult> transactions = response.transactions().stream()
                .map(this::toTransactionSummaryResult)
                .toList();

        return new TransactionQueryResult(
                response.totalElements(),
                response.totalPages(),
                response.last(),
                transactions
        );
    }

    public TransactionSummaryResult toTransactionSummaryResult(FinApiTransactionItemResponse response) {
        return new TransactionSummaryResult(
                response.transactionId(),
                response.transactionDate(),
                response.transactionType(),
                response.amount(),
                response.balanceAfter(),
                response.counterpartyName(),
                response.category()
        );
    }

    public TransactionDetailResult toTransactionDetailResult(FinApiTransactionDetailResponse response) {
        return new TransactionDetailResult(
                response.transactionId(),
                response.accountId(),
                response.transactionDate(),
                response.transactionType(),
                response.amount(),
                response.balanceAfter(),
                response.counterpartyName(),
                response.counterpartyBank(),
                response.counterpartyAccount(),
                response.fee(),
                response.category(),
                response.memo()
        );
    }

    public TransferResult toTransferResult(FinApiTransferResponse response) {
        return new TransferResult(
                response.transactionId(),
                response.depositAccountName(),
                response.depositBankName(),
                response.amount(),
                response.fee(),
                response.withdrawalBalance()
        );
    }

    public OneWonVerificationRequestResult toOneWonVerificationRequestResult(FinApiOneWonRequestResponse response) {
        return new OneWonVerificationRequestResult(
                response.verificationId(),
                response.bankName(),
                response.expiredIn()
        );
    }

    public OneWonVerificationConfirmResult toOneWonVerificationConfirmResult(FinApiOneWonConfirmResponse response) {
        return new OneWonVerificationConfirmResult(
                response.verified(),
                response.consentToken()
        );
    }

}
