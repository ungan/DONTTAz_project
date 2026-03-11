package com.donttaz.donttazapi.account.service;

import com.donttaz.donttazapi.account.dto.request.CreateAccountRequest;
import com.donttaz.donttazapi.account.dto.response.CreateAccountResponse;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountCreationService {

    private final DemandDepositService demandDepositService;

    public AccountCreationService(DemandDepositService demandDepositService) {
        this.demandDepositService = demandDepositService;
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        DemandDepositAccountCreateResult result = demandDepositService.createAccount(request.accountTypeUniqueNo());

        return CreateAccountResponse.builder()
                .bankCode(result.bankCode())
                .accountNo(result.accountNo())
                .currency(result.currency())
                .currencyName(result.currencyName())
                .build();
    }
}
