# infra.banking 구현 정리

## 문서 범위

이 문서는 `infra.banking` 패키지를 실제 동작하도록 구현할 때 필요한 기준만 정리한다.
이 문서에서 다루는 범위는 이전 답변의 `1번`, 즉 `infra.banking`을 어떻게 구현해야 하는지이다.

## 현재 코드 기준 확인된 상태

- 외부 금융 연동 계약은 `BankingClient`에 정의돼 있다.
- 실제 finAPI 연동 구현 위치는 `FinApiBankingClient`다.
- 외부 설정은 `FinApiProperties`가 `fin-api.base-url`, `fin-api.api-key`를 바인딩한다.
- finAPI 요청/응답 전용 DTO는 `infra.banking.dto.request`, `infra.banking.dto.response`에 분리돼 있다.
- 외부 응답을 서비스 내부 DTO로 변환하는 책임은 `FinApiBankingMapper`가 가진다.
- `FinApiBankingClient`의 메서드는 현재 전부 `UnsupportedOperationException` 상태다.

즉, 패키지 구조는 이미 준비되어 있고 실제 HTTP 호출과 예외 매핑만 비어 있다.

## 구현 목표

`infra.banking`의 목표는 `account` 서비스가 외부 API 형식에 직접 의존하지 않게 만드는 것이다.

의도된 호출 흐름은 아래와 같다.

`account service -> BankingClient -> FinApiBankingClient -> finAPI`

응답 변환 흐름은 아래와 같다.

`finAPI response DTO -> FinApiBankingMapper -> account.service.dto`

## 구현 시 지켜야 할 원칙

### 1. 외부 DTO를 서비스 계층에 직접 노출하지 않는다

- `FinApiAccountListResponse` 같은 외부 응답 객체는 `FinApiBankingClient` 내부에서만 사용한다.
- 서비스 계층에는 `AccountSummaryResult`, `TransferResult` 같은 내부 DTO만 반환한다.

### 2. 실패 처리는 `infra.banking`에서 끝낸다

- 네트워크 오류
- 타임아웃
- 역직렬화 실패
- finAPI 비정상 응답 코드

위 경우는 `CustomException(ErrorCode.FIN_API_REQUEST_FAILED)`로 변환하는 것이 현재 구조와 맞다.

### 3. URI, HTTP 메서드, 헤더 규격은 finAPI 명세 확인 후 확정한다

현재 저장소에는 실제 finAPI 명세 문서가 없으므로 아래 구현 순서는 구조 기준이다.
정확한 경로와 헤더 이름은 확정 사실처럼 하드코딩하면 안 된다.

## 구현 대상 파일

- `src/main/java/com/donttaz/donttazapi/infra/banking/client/FinApiBankingClient.java`

필요하면 아래 성격의 private helper를 같은 파일에 추가한다.

- 공통 요청 실행 helper
- 공통 응답 코드 검증 helper
- 공통 예외 변환 helper

## 메서드별 구현 방식

### getAccounts(Long userId)

목적:
사용자의 외부 계좌 목록을 finAPI에서 조회한다.

입력 조립:

- `FinApiAccountListRequest(userId)` 생성

처리 순서:

1. finAPI 계좌 목록 조회 엔드포인트를 호출한다.
2. 응답을 `FinApiAccountListResponse`로 받는다.
3. `responseCode`가 성공인지 검증한다.
4. `accounts`를 `finApiBankingMapper.toAccountSummaryResults(...)`로 변환한다.
5. `List<AccountSummaryResult>`를 반환한다.

주의:

- `accounts`가 `null`이면 빈 리스트 처리 여부를 명확히 정해야 한다.
- 성공 코드 규칙은 finAPI 명세 확인 후 helper에서 일관되게 처리하는 편이 낫다.

### getAccountDetail(Long userId, String externalAccountId)

목적:
외부 계좌 식별자로 단건 상세 정보를 조회한다.

처리 순서:

1. `userId`, `externalAccountId`를 finAPI 요청 형식에 맞게 전달한다.
2. 응답을 `FinApiAccountDetailResponse`로 받는다.
3. `responseCode`를 검증한다.
4. `finApiBankingMapper.toAccountDetailResult(...)`로 변환한다.
5. `AccountDetailResult`를 반환한다.

주의:

- `externalAccountId`는 내부 DB의 `accountId`와 다르다.
- 내부 서비스에서 외부 계좌 식별자를 어디서 가져오는지는 별도 연결 작업이 필요하다.

### getTransactions(...)

목적:
외부 계좌의 거래내역 목록을 조건별로 조회한다.

입력 조립:

- `FinApiTransactionListRequest(userId, externalAccountId, startDate, endDate, type, page, size)` 생성

처리 순서:

1. 날짜, 거래유형, 페이지 정보를 finAPI 요청 규격에 맞게 조립한다.
2. 응답을 `FinApiTransactionListResponse`로 받는다.
3. `responseCode`를 검증한다.
4. `finApiBankingMapper.toTransactionQueryResult(...)`로 변환한다.
5. `TransactionQueryResult`를 반환한다.

주의:

- `type` 값은 내부 enum 이름과 외부 API 코드 체계가 다를 수 있다.
- 다르면 `FinApiBankingClient` 내부에서 변환 규칙을 둬야 한다.

### getTransactionDetail(Long userId, String externalAccountId, String externalTransactionId)

목적:
외부 거래 한 건의 상세 정보를 조회한다.

처리 순서:

1. 계좌 식별자와 거래 식별자를 함께 전달한다.
2. 응답을 `FinApiTransactionDetailResponse`로 받는다.
3. `responseCode`를 검증한다.
4. `finApiBankingMapper.toTransactionDetailResult(...)`로 변환한다.
5. `TransactionDetailResult`를 반환한다.

### transfer(Long userId, TransferCommand command)

목적:
출금 계좌에서 타 계좌로 이체 요청을 보낸다.

입력 조립:

- `TransferCommand`를 `FinApiTransferRequest`로 변환한다.
- 현재 DTO 기준 전달 필드는 아래와 같다.
  - `userId`
  - `withdrawalAccountId`
  - `depositBankCode`
  - `depositAccountNumber`
  - `amount`
  - `withdrawalMemo`
  - `depositMemo`
  - `pinToken`

처리 순서:

1. `FinApiTransferRequest`를 생성한다.
2. finAPI 이체 엔드포인트를 호출한다.
3. 응답을 `FinApiTransferResponse`로 받는다.
4. `responseCode`를 검증한다.
5. `finApiBankingMapper.toTransferResult(...)`로 변환한다.
6. `TransferResult`를 반환한다.

주의:

- `withdrawalAccountId`가 내부 계좌 ID인지 외부 계좌 ID인지 현재 DTO 이름만으로는 불명확하다.
- 실제 구현 전 이 필드 의미를 먼저 확정해야 한다.

### requestOneWonVerification(Long userId, OneWonVerificationRequestCommand command)

목적:
1원 송금 인증 요청을 생성한다.

입력 조립:

- `FinApiOneWonRequest(userId, bankCode, accountNumber)` 생성

처리 순서:

1. finAPI 1원 인증 요청 엔드포인트를 호출한다.
2. 응답을 `FinApiOneWonRequestResponse`로 받는다.
3. `responseCode`를 검증한다.
4. `finApiBankingMapper.toOneWonVerificationRequestResult(...)`로 변환한다.
5. `OneWonVerificationRequestResult`를 반환한다.

### confirmOneWonVerification(Long userId, OneWonVerificationConfirmCommand command)

목적:
1원 인증 코드 확인 후 동의 토큰을 받는다.

입력 조립:

- `FinApiOneWonConfirmRequest(userId, verificationId, authCode)` 생성

처리 순서:

1. finAPI 1원 인증 확인 엔드포인트를 호출한다.
2. 응답을 `FinApiOneWonConfirmResponse`로 받는다.
3. `responseCode`를 검증한다.
4. `finApiBankingMapper.toOneWonVerificationConfirmResult(...)`로 변환한다.
5. `OneWonVerificationConfirmResult`를 반환한다.

## 권장 구현 형태

`FinApiBankingClient`에는 메서드마다 `try-catch`를 반복해서 늘어놓기보다 공통 helper를 두는 편이 낫다.

예시 책임 분리는 아래 정도가 적절하다.

- `buildHeaders()`
  - `apiKey` 같은 공통 헤더 구성
- `validateSuccess(String responseCode, String responseMessage)`
  - finAPI 성공 코드 검증
- `mapRequestFailure(Exception exception)`
  - `RestClientException`, `RuntimeException`을 `CustomException`으로 변환

실제 코드 형태는 대략 아래 구조가 된다.

```java
public List<AccountSummaryResult> getAccounts(Long userId) {
    FinApiAccountListRequest request = new FinApiAccountListRequest(userId);

    try {
        FinApiAccountListResponse response = callFinApi(...);
        validateSuccess(response.responseCode(), response.responseMessage());
        return finApiBankingMapper.toAccountSummaryResults(response.accounts());
    } catch (Exception exception) {
        throw toFinApiException(exception);
    }
}
```

위 코드는 형태 예시일 뿐이고, 실제 URI와 HTTP 메서드는 finAPI 명세에 맞춰야 한다.

## 구현 전에 먼저 확정해야 하는 항목

아래는 아직 코드만으로 확정할 수 없다.

- finAPI 실제 엔드포인트 경로
- 각 API의 HTTP 메서드
- 인증 헤더 이름과 형식
- 성공 `responseCode` 값
- 외부 계좌 ID와 내부 계좌 ID 매핑 방식
- 외부 거래 유형 코드와 내부 `TransactionType` 매핑 규칙

이 항목이 정리되지 않으면 `FinApiBankingClient`는 동작하더라도 나중에 다시 뜯어고칠 가능성이 높다.

## 구현 완료 기준

아래가 충족되면 `infra.banking` 1차 구현 완료로 볼 수 있다.

1. `FinApiBankingClient`의 `UnsupportedOperationException`이 제거된다.
2. 각 메서드가 finAPI 응답 DTO를 내부 서비스 DTO로 변환해서 반환한다.
3. 외부 호출 실패가 `CustomException(ErrorCode.FIN_API_REQUEST_FAILED)`로 일관되게 매핑된다.
4. `apiKey`, `baseUrl` 설정이 `FinApiProperties`를 통해 주입된다.
5. 성공 코드 검증 로직이 메서드별로 중복되지 않는다.

## 지금 시점의 결론

현재 `infra.banking`은 "외부 금융 연동용 구조는 준비됐지만 실제 HTTP 호출은 비어 있는 상태"다.
실제 구현은 새 레이어를 추가하는 작업이 아니라, 이미 만들어진 `FinApiBankingClient`에 호출 로직과 실패 매핑을 채워 넣는 작업으로 보는 것이 맞다.
