# 계좌 외부API 구현 계획

## 문서 목적

이 문서는 계좌 외부API 연동 작업의 현재 구현 상태와 남은 작업 범위를 정리한다.
현재 기준으로 조회성 API 일부는 구현 완료되었고, 입금/출금/이체는 아직 미구현 상태다.

대상 범위:

- 계좌 조회(단건)
- 예금주 조회
- 계좌 잔액 조회
- 계좌 출금
- 계좌 입금
- 계좌 이체

## 현재 구현 상태

### 완료된 범위

- `DemandDepositService`
  - 수시입출금 상품 등록/조회
  - 수시입출금 계좌 생성/목록 조회
- `AccountCreationService`
  - 계좌 생성 응답 조합
- `BankingAccountQueryService`
  - 계좌 단건 조회
  - 예금주 조회
  - 계좌 잔액 조회
- `AccountQueryService`
  - 계좌 목록 조회 응답 조합
  - 계좌 단건 조회 응답 조합
  - 예금주 조회 응답 조합
  - 계좌 잔액 조회 응답 조합
  - 거래내역 조회 응답 조합
- `FinApiBankingClient`
  - 공통 Header 생성
  - 공통 POST 호출
  - 성공 코드 검증
  - 예외 매핑
  - 계좌 단건 조회 구현
  - 예금주 조회 구현
  - 계좌 잔액 조회 구현
- Swagger
  - `AccountSwagger` 기준 문서 분리
  - 계좌 단건 조회, 예금주 조회, 계좌 잔액 조회 문서 반영

### 아직 남은 범위

- 계좌 출금 구현
- 계좌 입금 구현
- 계좌 이체 구현
- 거래내역 finAPI 전환
- 거래성 API 전용 서비스 분리

## 현재 구조 기준

요청 흐름:

`controller -> service -> BankingClient -> FinApiBankingClient -> finAPI`

응답 변환 흐름:

`finAPI response DTO -> FinApiBankingMapper -> account.service.dto -> controller response`

## 서비스 책임 분리 기준

### `DemandDepositService`

역할:

- 수시입출금 상품 등록/조회
- 수시입출금 계좌 생성/목록 조회

주의:

- 조회성 API와 거래성 API를 계속 이 서비스에 붙이지 않는다.
- demand deposit 자체 기능만 담당한다.

### `AccountCreationService`

역할:

- 계좌 생성 유스케이스 조합

주의:

- 현재 책임은 계좌 생성이다.
- 이후 입금/출금/이체가 추가돼도 이 서비스에 포함하지 않는다.

### `BankingAccountQueryService`

역할:

- 계좌 단건 조회
- 예금주 조회
- 계좌 잔액 조회

현재 상태:

- 조회성 API 3개 구현 완료
- finAPI 조회 기능은 이 서비스로 계속 모은다.

### `AccountTransferService`

역할:

- 계좌 출금
- 계좌 입금
- 계좌 이체

현재 상태:

- 아직 생성 전
- 거래성 API 구현 시점에 새로 추가한다.

### `AccountQueryService`

역할:

- 컨트롤러 응답 조합
- 조회 유스케이스 조합

주의:

- 현재는 거래내역 조회가 로컬 DB 기반이다.
- 조회성 finAPI 기능은 `BankingAccountQueryService`를 통해 호출한다.

## 식별자 기준

조회성 계좌 API는 `accountNo` 기준으로 맞춘다.

현재 이유:

- 계좌 단건 조회, 예금주 조회, 잔액 조회가 모두 외부API 계좌번호 기준이다.
- 이후 이체, 입금, 출금도 외부API 계좌번호 기준으로 설계될 가능성이 높다.

주의:

- 거래내역 API는 아직 `accountId` 기반 로컬 DB 조회다.
- 거래내역을 finAPI로 전환할 때 식별자 기준을 함께 정리해야 한다.

## 변경 대상 파일

### 현재까지 반영된 주요 파일

- `src/main/java/com/donttaz/donttazapi/account/controller/AccountController.java`
- `src/main/java/com/donttaz/donttazapi/account/controller/docs/AccountSwagger.java`
- `src/main/java/com/donttaz/donttazapi/account/service/AccountCreationService.java`
- `src/main/java/com/donttaz/donttazapi/account/service/AccountQueryService.java`
- `src/main/java/com/donttaz/donttazapi/account/service/BankingAccountQueryService.java`
- `src/main/java/com/donttaz/donttazapi/account/dto/response`
- `src/main/java/com/donttaz/donttazapi/account/service/dto`
- `src/main/java/com/donttaz/donttazapi/infra/banking/client/BankingClient.java`
- `src/main/java/com/donttaz/donttazapi/infra/banking/client/FinApiBankingClient.java`
- `src/main/java/com/donttaz/donttazapi/infra/banking/mapper/FinApiBankingMapper.java`
- `src/main/java/com/donttaz/donttazapi/infra/banking/dto/request`
- `src/main/java/com/donttaz/donttazapi/infra/banking/dto/response`
- `src/main/java/com/donttaz/donttazapi/common/config/SwaggerConfig.java`

### 다음 작업 대상

- `src/main/java/com/donttaz/donttazapi/account/service/AccountTransferService.java`
- `src/main/java/com/donttaz/donttazapi/account/dto/request`
- `src/main/java/com/donttaz/donttazapi/account/service/dto`
- `src/main/java/com/donttaz/donttazapi/infra/banking/dto/request`
- `src/main/java/com/donttaz/donttazapi/infra/banking/dto/response`

## 기능별 구현 계획

### 1. 계좌 조회(단건)

상태:

- 구현 완료

흐름:

- `GET /api/v1/accounts/{accountNo}`
- `AccountController`
- `AccountQueryService`
- `BankingAccountQueryService`
- `FinApiBankingClient.getAccountDetail(...)`

### 2. 예금주 조회

상태:

- 구현 완료

흐름:

- `GET /api/v1/accounts/{accountNo}/holder-name`
- `AccountController`
- `AccountQueryService`
- `BankingAccountQueryService`
- `FinApiBankingClient.getAccountHolderName(...)`

### 3. 계좌 잔액 조회

상태:

- 구현 완료

흐름:

- `GET /api/v1/accounts/{accountNo}/balance`
- `AccountController`
- `AccountQueryService`
- `BankingAccountQueryService`
- `FinApiBankingClient.getAccountBalance(...)`

### 4. 계좌 출금

상태:

- 미구현

구현 방향:

1. 외부API 출금 명세 확정
2. `BankingClient` 계약 추가
3. 서비스 DTO 추가
4. finAPI 요청/응답 DTO 추가
5. `FinApiBankingClient` 구현
6. `AccountTransferService` 추가 및 연결
7. 컨트롤러/Swagger 반영

### 5. 계좌 입금

상태:

- 미구현

구현 방향:

1. 외부API 입금 명세 확정
2. `BankingClient` 계약 추가
3. 서비스 DTO 추가
4. finAPI 요청/응답 DTO 추가
5. `FinApiBankingClient` 구현
6. `AccountTransferService` 메서드 추가
7. 컨트롤러/Swagger 반영

### 6. 계좌 이체

상태:

- 미구현

구현 방향:

1. 외부API 이체 명세 확정
2. `TransferCommand`, `TransferResult`와 명세 비교
3. 출금 계좌, 입금 계좌, 금액, 메모 필드 정리
4. `FinApiBankingClient.transfer(...)` 구현
5. `AccountTransferService` 연결
6. 필요 시 예금주 조회와 사전 검증 연결
7. 컨트롤러/Swagger 반영

## 컨트롤러 및 Swagger 기준

### 컨트롤러

- 조회성 API는 현재 `AccountController`에서 처리한다.
- 거래성 API가 추가되면 별도 컨트롤러 분리를 검토할 수 있다.

### Swagger

- Swagger 문서는 `account/controller/docs` 패키지로 분리한다.
- 컨트롤러는 Swagger 인터페이스를 `implements` 하는 방식으로 유지한다.
- 공통 4xx/5xx 응답은 `SwaggerConfig` 기준으로 관리한다.

## 공통 처리 기준

### 외부API 연동 위치

- 외부 finAPI 호출은 계속 `infra.banking`에서 담당한다.
- 외부 요청/응답 DTO는 상위 계층에 직접 노출하지 않는다.

### 예외 처리

- finAPI 실패는 `infra.banking` 계층에서 `CustomException`으로 변환한다.
- 기본 매핑은 `ErrorCode.FIN_API_REQUEST_FAILED`를 사용한다.

### 공통 응답

- 컨트롤러 응답은 `ApiResponse`를 사용한다.
- 공통 예외 응답은 `GlobalExceptionHandler`를 사용한다.

## 테스트 기준

이번 범위에서는 새 테스트 코드를 추가하지 않는다.

주의:

- 구현 변경 후 최소한 컴파일이 깨지지 않는지 확인한다.
- 필요 시 기존 테스트 참조가 깨지지 않도록만 맞춘다.
- 별도 테스트 보강은 이후 단계에서 진행한다.

## 작업 전 확정 필요 항목

- 출금 URI, Method, 요청/응답 필드
- 입금 URI, Method, 요청/응답 필드
- 이체 URI, Method, 요청/응답 필드
- 출금/입금이 독립 API인지 여부
- 성공 `responseCode`
- 이체 전 예금주 조회 선행 여부

## 완료 기준

아래를 만족하면 이 문서 범위의 1차 작업이 완료된 것으로 본다.

1. 계좌 단건 조회, 예금주 조회, 잔액 조회가 `BankingAccountQueryService` 기준으로 정리된다.
2. 출금, 입금, 이체가 `AccountTransferService` 기준으로 정리된다.
3. `DemandDepositService`는 demand deposit 자체 기능만 담당한다.
4. 외부 DTO와 내부 DTO 분리 구조가 유지된다.
5. Swagger 문서 분리와 공통 설정 구조가 유지된다.
