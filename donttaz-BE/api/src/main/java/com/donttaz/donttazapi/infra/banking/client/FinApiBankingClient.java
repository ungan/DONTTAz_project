package com.donttaz.donttazapi.infra.banking.client;

import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountSummaryResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransactionDetailResult;
import com.donttaz.donttazapi.account.service.dto.TransactionQueryResult;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.infra.banking.config.FinApiProperties;
import com.donttaz.donttazapi.infra.banking.mapper.FinApiBankingMapper;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class FinApiBankingClient implements BankingClient {

    private final FinApiProperties finApiProperties;
    private final FinApiBankingMapper finApiBankingMapper;
    private final RestClient restClient;

    public FinApiBankingClient(FinApiProperties finApiProperties, FinApiBankingMapper finApiBankingMapper) {
        this.finApiProperties = finApiProperties;
        this.finApiBankingMapper = finApiBankingMapper;
        this.restClient = RestClient.builder()
                .baseUrl(finApiProperties.getBaseUrl())
                .build();
    }

    @Override
    public List<AccountSummaryResult> getAccounts(Long userId) {
        throw new UnsupportedOperationException("finAPI account list integration is not implemented yet.");
    }

    @Override
    public AccountDetailResult getAccountDetail(Long userId, String externalAccountId) {
        throw new UnsupportedOperationException("finAPI account detail integration is not implemented yet.");
    }

    @Override
    public TransactionQueryResult getTransactions(Long userId, String externalAccountId, LocalDate startDate,
                                                  LocalDate endDate, String type, int page, int size) {
        throw new UnsupportedOperationException("finAPI transaction list integration is not implemented yet.");
    }

    @Override
    public TransactionDetailResult getTransactionDetail(Long userId, String externalAccountId, String externalTransactionId) {
        throw new UnsupportedOperationException("finAPI transaction detail integration is not implemented yet.");
    }

    @Override
    public TransferResult transfer(Long userId, TransferCommand command) {
        throw new UnsupportedOperationException("finAPI transfer integration is not implemented yet.");
    }

    @Override
    public OneWonVerificationRequestResult requestOneWonVerification(Long userId, OneWonVerificationRequestCommand command) {
        throw new UnsupportedOperationException("finAPI one-won request integration is not implemented yet.");
    }

    @Override
    public OneWonVerificationConfirmResult confirmOneWonVerification(Long userId, OneWonVerificationConfirmCommand command) {
        throw new UnsupportedOperationException("finAPI one-won confirm integration is not implemented yet.");
    }

}
