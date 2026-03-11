package com.donttaz.donttazapi.infra.banking.client;

import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountSummaryResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositProductResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import java.time.LocalDate;
import java.util.List;

public interface BankingClient {

    DemandDepositProductResult createDemandDepositProduct(
            String bankCode,
            String accountName,
            String accountDescription
    );

    List<DemandDepositProductResult> getDemandDepositProducts();

    DemandDepositAccountCreateResult createDemandDepositAccount(Long userId, String accountTypeUniqueNo);

    List<DemandDepositAccountResult> getDemandDepositAccounts(Long userId);

    List<AccountSummaryResult> getAccounts(Long userId);

    AccountDetailResult getAccountDetail(Long userId, String externalAccountId);

    TransactionQueryResult getTransactions(
            Long userId,
            String externalAccountId,
            LocalDate startDate,
            LocalDate endDate,
            String type,
            int page,
            int size
    );

    TransactionDetailResult getTransactionDetail(Long userId, String externalAccountId, String externalTransactionId);

    TransferResult transfer(Long userId, TransferCommand command);

    OneWonVerificationRequestResult requestOneWonVerification(Long userId, OneWonVerificationRequestCommand command);

    OneWonVerificationConfirmResult confirmOneWonVerification(Long userId, OneWonVerificationConfirmCommand command);

    String createUserKey(String email);
}
