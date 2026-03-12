package com.donttaz.donttazapi.infra.banking.mapper;

import com.donttaz.donttazapi.account.service.dto.AccountBalanceResult;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.AccountHolderNameResult;
import com.donttaz.donttazapi.account.service.dto.AccountSummaryResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransactionSummaryResult;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountBalanceResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDepositResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDetailResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountHolderNameResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountWithdrawalResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountItemResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiCreateDemandDepositAccountRec;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiDemandDepositAccountRec;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiDemandDepositProductRec;
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

    public DemandDepositProductResult toDemandDepositProductResult(FinApiDemandDepositProductRec response) {
        return new DemandDepositProductResult(
                response.accountTypeUniqueNo(),
                response.bankCode(),
                response.bankName(),
                response.accountTypeCode(),
                response.accountTypeName(),
                response.accountName(),
                response.accountDescription(),
                response.accountType()
        );
    }

    public List<DemandDepositProductResult> toDemandDepositProductResults(List<FinApiDemandDepositProductRec> responses) {
        return responses.stream()
                .map(this::toDemandDepositProductResult)
                .toList();
    }

    public DemandDepositAccountCreateResult toDemandDepositAccountCreateResult(FinApiCreateDemandDepositAccountRec response) {
        return new DemandDepositAccountCreateResult(
                response.bankCode(),
                response.accountNo(),
                response.currency().currency(),
                response.currency().currencyName()
        );
    }

    public DemandDepositAccountResult toDemandDepositAccountResult(FinApiDemandDepositAccountRec response) {
        return new DemandDepositAccountResult(
                response.bankCode(),
                response.bankName(),
                response.userName(),
                response.accountNo(),
                response.accountName(),
                response.accountTypeCode(),
                response.accountTypeName(),
                response.accountCreatedDate(),
                response.accountExpiryDate(),
                response.dailyTransferLimit(),
                response.oneTimeTransferLimit(),
                response.accountBalance(),
                response.lastTransactionDate(),
                response.currency()
        );
    }

    public List<DemandDepositAccountResult> toDemandDepositAccountResults(List<FinApiDemandDepositAccountRec> responses) {
        return responses.stream()
                .map(this::toDemandDepositAccountResult)
                .toList();
    }

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
        FinApiDemandDepositAccountRec rec = response.rec();
        return new AccountDetailResult(
                rec.bankCode(),
                rec.bankName(),
                rec.userName(),
                rec.accountNo(),
                rec.accountName(),
                rec.accountTypeCode(),
                rec.accountTypeName(),
                rec.accountCreatedDate(),
                rec.accountExpiryDate(),
                rec.dailyTransferLimit(),
                rec.oneTimeTransferLimit(),
                rec.accountBalance(),
                rec.lastTransactionDate(),
                rec.currency()
        );
    }

    public AccountBalanceResult toAccountBalanceResult(FinApiAccountBalanceResponse response) {
        return new AccountBalanceResult(
                response.rec().bankCode(),
                response.rec().accountNo(),
                response.rec().accountBalance(),
                response.rec().accountCreatedDate(),
                response.rec().accountExpiryDate(),
                response.rec().lastTransactionDate(),
                response.rec().currency()
        );
    }

    public AccountHolderNameResult toAccountHolderNameResult(FinApiAccountHolderNameResponse response) {
        return new AccountHolderNameResult(
                response.rec().bankCode(),
                response.rec().bankName(),
                response.rec().accountNo(),
                response.rec().userName(),
                response.rec().currency()
        );
    }

    public AccountDepositResult toAccountDepositResult(FinApiAccountDepositResponse response) {
        return new AccountDepositResult(
                response.rec().transactionUniqueNo(),
                response.rec().transactionDate()
        );
    }

    public AccountWithdrawalResult toAccountWithdrawalResult(FinApiAccountWithdrawalResponse response) {
        return new AccountWithdrawalResult(
                response.rec().transactionUniqueNo(),
                response.rec().transactionDate()
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
