package com.donttaz.donttazapi.infra.banking.client;

import com.donttaz.donttazapi.account.service.dto.AccountBalanceResult;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.AccountHolderNameResult;
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
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.infra.banking.config.FinApiProperties;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiAccountDepositRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiAccountWithdrawalRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiCreateDemandDepositAccountRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiCreateDemandDepositRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiInquireDemandDepositAccountBalanceRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiInquireDemandDepositAccountHolderNameRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiInquireDemandDepositAccountListRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiInquireDemandDepositAccountRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiInquireDemandDepositListRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiMemberRequest;
import com.donttaz.donttazapi.infra.banking.dto.request.FinApiRequestHeader;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountBalanceResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDepositResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountDetailResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountHolderNameResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiAccountWithdrawalResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiCreateDemandDepositAccountResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiCreateDemandDepositResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiInquireDemandDepositAccountListResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiInquireDemandDepositListResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiMemberResponse;
import com.donttaz.donttazapi.infra.banking.dto.response.FinApiResponseHeader;
import com.donttaz.donttazapi.infra.banking.mapper.FinApiBankingMapper;
import com.donttaz.donttazapi.infra.banking.userkey.UserKeyProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FinApiBankingClient implements BankingClient {

    private static final Logger log = LoggerFactory.getLogger(FinApiBankingClient.class);
    private static final String EDU_API_PREFIX = "/v1/edu";
    private static final String DEMAND_DEPOSIT_CREATE_URI = "/demandDeposit/createDemandDeposit";
    private static final String DEMAND_DEPOSIT_LIST_URI = "/demandDeposit/inquireDemandDepositList";
    private static final String DEMAND_DEPOSIT_ACCOUNT_CREATE_URI = "/demandDeposit/createDemandDepositAccount";
    private static final String DEMAND_DEPOSIT_ACCOUNT_LIST_URI = "/demandDeposit/inquireDemandDepositAccountList";
    private static final String DEMAND_DEPOSIT_ACCOUNT_BALANCE_URI = "/demandDeposit/inquireDemandDepositAccountBalance";
    private static final String DEMAND_DEPOSIT_ACCOUNT_DETAIL_URI = "/demandDeposit/inquireDemandDepositAccount";
    private static final String DEMAND_DEPOSIT_ACCOUNT_HOLDER_NAME_URI = "/demandDeposit/inquireDemandDepositAccountHolderName";
    private static final String DEMAND_DEPOSIT_ACCOUNT_DEPOSIT_URI = "/demandDeposit/updateDemandDepositAccountDeposit";
    private static final String DEMAND_DEPOSIT_ACCOUNT_WITHDRAWAL_URI = "/demandDeposit/updateDemandDepositAccountWithdrawal";
    private static final String SUCCESS_CODE = "H0000";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter UNIQUE_NO_PREFIX_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final FinApiProperties finApiProperties;
    private final FinApiBankingMapper finApiBankingMapper;
    private final UserKeyProvider userKeyProvider;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public FinApiBankingClient(
            FinApiProperties finApiProperties,
            FinApiBankingMapper finApiBankingMapper,
            UserKeyProvider userKeyProvider,
            ObjectMapper objectMapper
    ) {
        this.finApiProperties = finApiProperties;
        this.finApiBankingMapper = finApiBankingMapper;
        this.userKeyProvider = userKeyProvider;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(finApiProperties.getBaseUrl())
                .build();
    }

    @Override
    public DemandDepositProductResult createDemandDepositProduct(
            String bankCode,
            String accountName,
            String accountDescription
    ) {
        FinApiCreateDemandDepositResponse response = post(
                resolveUri(DEMAND_DEPOSIT_CREATE_URI),
                new FinApiCreateDemandDepositRequest(
                        buildHeader("createDemandDeposit", null),
                        bankCode,
                        accountName,
                        accountDescription
                ),
                FinApiCreateDemandDepositResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toDemandDepositProductResult(response.rec());
    }

    @Override
    public List<DemandDepositProductResult> getDemandDepositProducts() {
        FinApiInquireDemandDepositListResponse response = post(
                resolveUri(DEMAND_DEPOSIT_LIST_URI),
                new FinApiInquireDemandDepositListRequest(buildHeader("inquireDemandDepositList", null)),
                FinApiInquireDemandDepositListResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toDemandDepositProductResults(defaultList(response.rec()));
    }

    @Override
    public DemandDepositAccountCreateResult createDemandDepositAccount(Long userId, String accountTypeUniqueNo) {
        FinApiCreateDemandDepositAccountResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_CREATE_URI),
                new FinApiCreateDemandDepositAccountRequest(
                        buildHeader("createDemandDepositAccount", userId),
                        accountTypeUniqueNo
                ),
                FinApiCreateDemandDepositAccountResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toDemandDepositAccountCreateResult(response.rec());
    }

    @Override
    public List<DemandDepositAccountResult> getDemandDepositAccounts(Long userId) {
        FinApiInquireDemandDepositAccountListResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_LIST_URI),
                new FinApiInquireDemandDepositAccountListRequest(buildHeader("inquireDemandDepositAccountList", userId)),
                FinApiInquireDemandDepositAccountListResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toDemandDepositAccountResults(defaultList(response.rec()));
    }

    @Override
    public List<AccountSummaryResult> getAccounts(Long userId) {
        throw new UnsupportedOperationException("finAPI account list integration is not implemented yet.");
    }

    @Override
    public AccountBalanceResult getAccountBalance(Long userId, String accountNo) {
        FinApiAccountBalanceResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_BALANCE_URI),
                new FinApiInquireDemandDepositAccountBalanceRequest(
                        buildHeader("inquireDemandDepositAccountBalance", userId),
                        accountNo
                ),
                FinApiAccountBalanceResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toAccountBalanceResult(response);
    }

    @Override
    public AccountDetailResult getAccountDetail(Long userId, String accountNo) {
        FinApiAccountDetailResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_DETAIL_URI),
                new FinApiInquireDemandDepositAccountRequest(
                        buildHeader("inquireDemandDepositAccount", userId),
                        accountNo
                ),
                FinApiAccountDetailResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toAccountDetailResult(response);
    }

    @Override
    public AccountHolderNameResult getAccountHolderName(Long userId, String accountNo) {
        FinApiAccountHolderNameResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_HOLDER_NAME_URI),
                new FinApiInquireDemandDepositAccountHolderNameRequest(
                        buildHeader("inquireDemandDepositAccountHolderName", userId),
                        accountNo
                ),
                FinApiAccountHolderNameResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toAccountHolderNameResult(response);
    }

    @Override
    public AccountDepositResult deposit(Long userId, AccountDepositCommand command) {
        FinApiAccountDepositResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_DEPOSIT_URI),
                new FinApiAccountDepositRequest(
                        buildHeader("updateDemandDepositAccountDeposit", userId),
                        command.accountNo(),
                        command.transactionBalance(),
                        command.transactionSummary()
                ),
                FinApiAccountDepositResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toAccountDepositResult(response);
    }

    @Override
    public AccountWithdrawalResult withdrawal(Long userId, AccountWithdrawalCommand command) {
        FinApiAccountWithdrawalResponse response = post(
                resolveUri(DEMAND_DEPOSIT_ACCOUNT_WITHDRAWAL_URI),
                new FinApiAccountWithdrawalRequest(
                        buildHeader("updateDemandDepositAccountWithdrawal", userId),
                        command.accountNo(),
                        command.transactionBalance(),
                        command.transactionSummary()
                ),
                FinApiAccountWithdrawalResponse.class
        );

        validateSuccess(response.header());
        return finApiBankingMapper.toAccountWithdrawalResult(response);
    }

    @Override
    public TransactionQueryResult getTransactions(
            Long userId,
            String externalAccountId,
            LocalDate startDate,
            LocalDate endDate,
            String type,
            int page,
            int size
    ) {
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

    @Override
    public String createUserKey(String email) {
        try {
            FinApiMemberRequest request = new FinApiMemberRequest(finApiProperties.getApiKey(), email);

            FinApiMemberResponse response = restClient.post()
                    .uri("/v1/member")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(FinApiMemberResponse.class);

            if (response == null || response.userKey() == null) {
                throw new CustomException(ErrorCode.USER_KEY_ISSUANCE_FAILED);
            }

            return response.userKey();
        } catch (RestClientException exception) {
            throw new CustomException(ErrorCode.USER_KEY_ISSUANCE_FAILED, exception.getMessage());
        }
    }

    private FinApiRequestHeader buildHeader(String apiName, Long userId) {
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);

        return new FinApiRequestHeader(
                apiName,
                now.format(DATE_FORMATTER),
                now.format(TIME_FORMATTER),
                finApiProperties.getInstitutionCode(),
                finApiProperties.getFintechAppNo(),
                apiName,
                buildInstitutionTransactionUniqueNo(now),
                finApiProperties.getApiKey(),
                resolveUserKey(userId)
        );
    }

    private String resolveUserKey(Long userId) {
        if (userId == null) {
            return null;
        }

        String userKey = userKeyProvider.getUserKey(userId);
        if (userKey == null || userKey.isBlank()) {
            throw new CustomException(ErrorCode.FIN_API_REQUEST_FAILED, "userKey is not available.");
        }
        return userKey;
    }

    private String buildInstitutionTransactionUniqueNo(LocalDateTime now) {
        return now.format(UNIQUE_NO_PREFIX_FORMATTER)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    private void validateSuccess(FinApiResponseHeader header) {
        if (header == null) {
            throw new CustomException(ErrorCode.FIN_API_REQUEST_FAILED);
        }

        if (!SUCCESS_CODE.equals(header.responseCode())) {
            throw new CustomException(
                    ErrorCode.FIN_API_REQUEST_FAILED,
                    header.responseMessage() == null || header.responseMessage().isBlank()
                            ? ErrorCode.FIN_API_REQUEST_FAILED.getMessage()
                            : header.responseMessage()
            );
        }
    }

    private <T> T post(String uri, Object requestBody, Class<T> responseType) {
        try {
            log.info("finAPI request uri={} body={}", uri, toMaskedJson(requestBody));
            T response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(responseType);

            if (response == null) {
                throw new CustomException(ErrorCode.FIN_API_REQUEST_FAILED);
            }

            log.info("finAPI response uri={} body={}", uri, toMaskedJson(response));
            return response;
        } catch (RestClientResponseException exception) {
            log.warn(
                    "finAPI response error uri={} status={} body={}",
                    uri,
                    exception.getStatusCode(),
                    maskSensitiveValues(exception.getResponseBodyAsString())
            );
            throw new CustomException(ErrorCode.FIN_API_REQUEST_FAILED, exception.getResponseBodyAsString());
        } catch (RestClientException exception) {
            log.warn("finAPI request failed uri={} message={}", uri, exception.getMessage());
            throw new CustomException(ErrorCode.FIN_API_REQUEST_FAILED);
        }
    }

    private <T> List<T> defaultList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private String resolveUri(String resourcePath) {
        String baseUrl = finApiProperties.getBaseUrl();
        if (baseUrl != null && baseUrl.contains(EDU_API_PREFIX)) {
            return resourcePath;
        }
        return EDU_API_PREFIX + resourcePath;
    }

    private String toMaskedJson(Object value) {
        try {
            return maskSensitiveValues(objectMapper.writeValueAsString(value));
        } catch (JsonProcessingException exception) {
            return "<json-serialization-failed>";
        }
    }

    private String maskSensitiveValues(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        return value
                .replaceAll("(\"apiKey\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3")
                .replaceAll("(\"userKey\"\\s*:\\s*\")([^\"]+)(\")", "$1***$3");
    }
}
