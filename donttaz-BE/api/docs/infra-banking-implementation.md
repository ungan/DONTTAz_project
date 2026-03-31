# infra.banking 구현 정리

## 문서 범위

이 문서는 `infra.banking` 패키지의 역할, 책임, 공통 처리 기준을 정리한다.
계좌 기능의 진행 현황과 상세 구현 계획은 `account-banking-implementation-plan.md`에서 관리한다.

## 현재 구조

외부 연동 흐름:

`controller -> service -> BankingClient -> FinApiBankingClient -> finAPI`

응답 변환 흐름:

`finAPI response DTO -> FinApiBankingMapper -> account.service.dto`

현재 `infra.banking`은 아래 역할을 담당한다.

- 외부API 요청 Header 생성
- finAPI POST 호출
- 성공 코드 검증
- 외부API 예외 매핑
- 외부 요청/응답 DTO 관리
- 외부 응답을 내부 서비스 DTO로 변환

## 현재 구현 상태

### 구현 완료

- `FinApiBankingClient.createDemandDepositProduct(...)`
- `FinApiBankingClient.getDemandDepositProducts()`
- `FinApiBankingClient.createDemandDepositAccount(...)`
- `FinApiBankingClient.getDemandDepositAccounts(...)`
- `FinApiBankingClient.getAccountDetail(...)`
- `FinApiBankingClient.getAccountHolderName(...)`
- `FinApiBankingClient.getAccountBalance(...)`

### 미구현

- `FinApiBankingClient.getAccounts(...)`
- `FinApiBankingClient.getTransactions(...)`
- `FinApiBankingClient.getTransactionDetail(...)`
- `FinApiBankingClient.transfer(...)`
- `FinApiBankingClient.requestOneWonVerification(...)`
- `FinApiBankingClient.confirmOneWonVerification(...)`

## 패키지 책임

### `client`

역할:

- 외부API 실제 호출
- URI 관리
- 공통 Header 생성
- 공통 성공 코드 검증
- 공통 예외 매핑

현재 구현체:

- `BankingClient`
- `FinApiBankingClient`

### `dto.request`

역할:

- finAPI 요청 바디 모델 정의

원칙:

- 요청 DTO는 외부API 엔드포인트별로 분리한다.
- 구조가 같아도 디버깅과 유지보수를 위해 API 단위 이름을 유지한다.

### `dto.response`

역할:

- finAPI 응답 바디 모델 정의

원칙:

- `Header`, `REC` 구조를 그대로 반영한다.
- 상위 계층으로 직접 노출하지 않는다.

### `mapper`

역할:

- 외부 응답 DTO를 내부 서비스 DTO로 변환

원칙:

- 컨트롤러나 서비스 상위 계층에서 finAPI DTO를 직접 사용하지 않는다.
- 변환 책임은 `FinApiBankingMapper`에 둔다.

### `config`

역할:

- `FinApiProperties`를 통한 외부API 설정 주입

주요 설정:

- `fin-api.base-url`
- `fin-api.api-key`
- `fin-api.institution-code`
- `fin-api.fintech-app-no`
- `fin-api.mock-user-key`

### `userkey`

역할:

- 사용자 기준 `userKey` 제공

현재 상태:

- 인증 전이라 임시 사용자 기준 처리

## 공통 구현 원칙

### 1. 외부 DTO를 상위 계층에 노출하지 않는다

- `FinApi...Request`, `FinApi...Response`는 `infra.banking` 내부에서만 사용한다.
- 서비스 계층에는 `AccountDetailResult`, `AccountHolderNameResult`, `AccountBalanceResult` 같은 내부 DTO만 전달한다.

### 2. 실패 처리는 `infra.banking`에서 일관되게 변환한다

대상:

- 네트워크 오류
- 타임아웃
- 직렬화 실패
- finAPI 비정상 응답
- finAPI 실패 `responseCode`

처리 기준:

- `CustomException(ErrorCode.FIN_API_REQUEST_FAILED)`로 변환한다.

### 3. 공통 Header 생성 로직은 한 곳에서 관리한다

현재 기준:

- `buildHeader(String apiName, Long userId)`

포함 값:

- `apiName`
- `transmissionDate`
- `transmissionTime`
- `institutionCode`
- `fintechAppNo`
- `apiServiceCode`
- `institutionTransactionUniqueNo`
- `apiKey`
- `userKey`

### 4. 성공 코드 검증은 공통 처리한다

현재 기준:

- `validateSuccess(FinApiResponseHeader header)`

성공 코드:

- `H0000`

### 5. 공통 POST 호출 로직을 재사용한다

현재 기준:

- `post(String uri, Object requestBody, Class<T> responseType)`

역할:

- 요청 로그
- 응답 로그
- JSON 직렬화 예외 처리
- `RestClientException` 계열 예외 변환

## 구현 시 주의 사항

- 외부API URI, Method, 요청 필드는 명세 기준으로만 확정한다.
- 추측성 필드 추가나 임시 하드코딩은 최소화한다.
- `accountId`와 `accountNo`를 혼동하지 않는다.
- 거래성 API는 조회성 API와 달리 실패 비용이 크므로 검증과 응답 코드 확인을 먼저 챙긴다.
- 외부 응답 구조 변경 가능성이 있으므로 DTO와 매퍼를 분리한 상태를 유지한다.

## 다음 작업

`infra.banking` 관점의 다음 작업은 아래와 같다.

- 출금 요청/응답 DTO 추가
- 입금 요청/응답 DTO 추가
- 이체 요청/응답 DTO 추가
- `transfer(...)` 구현
- 거래내역 조회용 DTO 및 메서드 구현
- 1원 인증 요청/확인 구현

## 완료 기준

아래를 만족하면 `infra.banking` 1차 구현이 완료된 것으로 본다.

1. 외부API 호출 메서드가 `UnsupportedOperationException` 없이 동작한다.
2. 외부 요청/응답 DTO와 내부 서비스 DTO 분리 구조가 유지된다.
3. 성공 코드 검증과 예외 매핑이 공통 로직으로 유지된다.
4. 설정값은 `FinApiProperties`를 통해 주입된다.
5. 서비스 계층이 finAPI 세부 구조를 직접 알지 않아도 된다.
