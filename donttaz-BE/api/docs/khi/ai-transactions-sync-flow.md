# `/api/v1/ai/transactions/sync` 동작 로직 정리

이 문서는 백엔드 코드 기준으로 `POST /api/v1/ai/transactions/sync` 엔드포인트가 어떤 순서와 조건으로 동작하는지 정리한 문서다.

## 1. 엔드포인트 개요

- 경로: `POST /api/v1/ai/transactions/sync`
- 컨트롤러: `AiTransactionController.syncTransactions()`
- 실제 핵심 로직: `AiTransactionService.syncTransactions()`
- 목적:
  - 현재 로그인 사용자의 연결 계좌 목록을 조회한다.
  - 각 계좌별로 외부 금융 거래내역 API를 조회한다.
  - DB에 없는 거래만 신규 저장한다.
  - 신규 저장 거래와 아직 AI 분류가 안 된 기존 거래를 AI 서버에 보내 카테고리 분류 메타데이터를 저장한다.

## 2. 진입 지점

컨트롤러에서 `request`가 없으면 `new AiTransactionSyncRequest(null, null)`로 대체한 뒤 서비스에 위임한다.

```java
@PostMapping("/ai/transactions/sync")
public ApiResponse<AiTransactionSyncResponse> syncTransactions(@RequestBody(required = false) AiTransactionSyncRequest request) {
    if (request == null) {
        request = new AiTransactionSyncRequest(null, null);
    }
    AiTransactionSyncResponse response = aiTransactionService.syncTransactions(request);
    return ApiResponse.success("최신 거래 내역 수집이 완료되었습니다.", response);
}
```

## 3. 요청값 의미

요청 DTO는 아래 2개 필드만 가진다.

```json
{
  "startDate": "2026-02-01",
  "endDate": "2026-03-05"
}
```

| 필드 | 타입 | 의미 | 기본값 |
| :--- | :--- | :--- | :--- |
| `startDate` | `LocalDate` | 거래 조회 시작일 | 없으면 `오늘 - 3개월` |
| `endDate` | `LocalDate` | 거래 조회 종료일 | 없으면 `오늘` |

주의:
- 이 기간은 외부 금융 API 조회 구간에만 직접 적용된다.
- 뒤쪽의 "기존 미분석 거래 재분석" 단계는 기간 제한 없이 사용자의 전체 거래를 다시 훑는다.

## 4. 전체 처리 흐름

서비스 메서드 `AiTransactionService.syncTransactions()`의 큰 흐름은 아래와 같다.

1. 현재 로그인 사용자 ID를 조회한다.
2. 해당 사용자의 연결 계좌 목록을 조회한다.
3. 요청 기간을 계산한다.
4. 각 계좌에 대해 외부 금융 API에서 거래내역을 조회한다.
5. DB에 없는 거래만 신규 저장한다.
6. 신규 저장 거래가 있으면 AI 서버로 분류 요청을 보내 `transaction_ai_metadata`를 저장한다.
7. 그 다음, DB에 이미 존재하는 거래 중 아직 AI 메타데이터가 없거나 카테고리가 `"미분류"`인 거래를 다시 모은다.
8. 위 거래들을 다시 AI 서버로 보내 분류 메타데이터를 저장한다.
9. 최종적으로 `syncedCount`, `lastSyncTime`을 응답한다.

## 5. 상세 로직

### 5.1 사용자 및 계좌 조회

- `currentUserProvider.getCurrentUserId()`로 로그인 사용자 ID를 가져온다.
- `accountRepository.findByUserId(userId)`로 사용자의 계좌 목록을 가져온다.

즉, 이 API는 요청 본문으로 특정 계좌를 받지 않고, "현재 사용자에게 연결된 모든 계좌"를 대상으로 동작한다.

### 5.2 조회 기간 계산

서비스에서 기간은 아래처럼 계산된다.

```java
LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now().minusMonths(3);
LocalDate endDate = request.endDate() != null ? request.endDate() : LocalDate.now();
```

따라서 요청 본문이 비어 있으면 최근 3개월부터 오늘까지 조회한다.

### 5.3 계좌별 외부 거래 조회

각 계좌마다 `syncAccountTransactions(account, startDate, endDate)`를 호출한다.

이 메서드는 내부적으로 `BankingAccountQueryService.getTransactions()`를 사용한다.

```java
TransactionQueryResult queryResult = bankingAccountQueryService.getTransactions(
        account.getAccountNumber(),
        startDate,
        endDate,
        "M",
        1,
        100
);
```

핵심 포인트:
- 조회 대상은 계좌번호(`accountNumber`)
- 거래 타입은 `"M"` 고정
- 페이지는 `1`, 크기는 `100` 고정

즉 현재 구현상 계좌별로 외부 API 1페이지 100건만 가져온다. 기간 내 거래가 100건을 넘는 경우 추가 페이지를 반복 조회하지 않는다.

### 5.4 신규 거래 여부 판별

외부 API 결과의 각 거래에 대해 아래 조건으로 중복 여부를 확인한다.

```java
transactionRepository.existsByAccountAccountIdAndSourceAndSourceTxId(
    account.getAccountId(), "FIN_API", res.externalTransactionId()
)
```

중복 판별 키는 사실상 아래 3개 조합이다.

- `account_id`
- `source` = `"FIN_API"`
- `source_tx_id` = 외부 거래 고유 ID

이 조건은 `account_transaction` 테이블의 유니크 제약과도 맞물려 있다.

## 6. 신규 저장되는 거래 데이터

중복이 아니면 `Transaction` 엔티티를 생성해서 바로 저장한다.

주요 매핑은 아래와 같다.

| DB 필드 성격 | 저장 값 |
| :--- | :--- |
| `account` | 현재 순회 중인 계좌 |
| `userId` | 계좌의 사용자 ID |
| `source` | `"FIN_API"` |
| `sourceTxId` | 외부 API의 `externalTransactionId` |
| `txType`, `txTypeName` | 외부 API의 `type` |
| `amount` | 외부 API의 `amount` |
| `balanceSnapshot` | 외부 API의 `balanceAfter` |
| `counterpartyName` | 외부 API의 `counterpartyName` |
| `transactionSummary` | 현재 구현상 `counterpartyName`과 동일하게 저장 |
| `transactionAt` | 외부 API의 `transactionDate` |
| `syncedAt` | 저장 시각 `LocalDateTime.now()` |

주의:
- `transactionSummary`에 별도 설명문이 아니라 `counterpartyName`이 그대로 들어간다.
- `Transaction` 엔티티에도 `aiCategory` 필드가 있지만, 실제 AI 분류 결과는 별도 테이블 `transaction_ai_metadata`에 저장한다.

## 7. 신규 거래 AI 분류

계좌별 신규 거래가 1건 이상이면 `analyzeAndSaveAiMetadata(newTransactions)`를 호출한다.

### 7.1 AI 서버 요청 payload

현재 백엔드 구현은 AI 서버에 아래 payload를 보낸다.

```json
{
  "transactions": [
    {
      "transactionId": "123",
      "transactionDate": "2026-03-01",
      "amount": 4500,
      "counterpartyName": "스타벅스",
      "type": "WITHDRAWAL"
    }
  ]
}
```

현재 생성 기준:
- `transactionId`: DB에 저장된 `Transaction.id`
- `transactionDate`: `transactionAt`의 날짜 부분
- `amount`: 금액
- `counterpartyName`: 거래처명
- `type`: `txType`

즉 현재 백엔드는 AI Swagger에서 직접 검증된 classify 요청 필드를 사용하면서, DB 매핑을 위해 `transactionId`를 추가로 함께 보낸다.
AI 서버의 classify 기준 스펙은 [`ai-server-api-spec.md`](/c:/ssafy/project2/BE_develop2/S14P21C104/donttaz-BE/api/docs/khi/ai-server-api-spec.md)를 기준으로 본다.

### 7.2 AI 서버 호출 주소

`AiServiceClient.analyzeTransactions()`에서 아래 URL로 POST 요청한다.

- `{ai.server.url}{ai.analysis.endpoint}`
- 기본 analysis endpoint: `/internal/ai/categories/classify`

즉 설정값을 풀면 보통 다음 형태다.

```text
POST {AI_SERVER_URL}/internal/ai/categories/classify
```

### 7.3 AI 응답 저장 방식

현재 백엔드는 AI 응답을 `transactionId` 기준으로 맵핑해 각 거래의 `TransactionAiMetadata`를 저장한다.

다만 실제 AI 서버 응답은 `data` 대신 `categories`, `confidence` 대신 `confidenceScore`를 반환하는 것이 확인되었다.
또한 AI Swagger 직접 실행 결과만 보면 응답의 `transactionId`가 요청의 DB id를 그대로 반영한다고 보장하기 어렵다.

따라서 응답 필드 정의와 매핑 기준은 구현 가정이 아니라 실제 AI 서버 스펙으로 다시 맞춰야 한다.

저장 필드:
- `transaction`
- `aiCategory`
- `confidenceScore`
- `isModified`는 builder 기본값으로 `false`
- `modifiedCategory`, `modifiedAt`는 비어 있음

AI가 특정 거래 결과를 안 주면 fallback으로 아래 값을 저장한다.
- `aiCategory = "미분류"`
- `confidenceScore = 0.0`

## 8. 기존 미분석 거래 재분석

이 API의 중요한 특징은 "외부에서 새로 가져온 거래만 처리하고 끝나지 않는다"는 점이다.

신규 저장 로직이 끝난 뒤, 아래 코드를 통해 사용자의 전체 거래를 다시 조회한다.

```java
List<Transaction> allTransactions = transactionRepository.findByUserId(userId);
```

그 후 아래 조건을 만족하면 재분석 대상으로 잡는다.

- `transaction_ai_metadata`가 아예 없음
- 또는 메타데이터는 있지만 `aiCategory == "미분류"`

즉 이 단계는 기간 제한을 쓰지 않는다. 과거 거래 전체가 대상이 될 수 있다.

대상 거래가 있으면 `analyzeAndSaveAiMetadata(unanalyzedTransactions)`를 다시 호출한다.

## 9. 사용자 수동 수정값과의 관계

카테고리 수동 수정은 `TransactionAiMetadata.updateCategory()`에서 아래 필드에 저장된다.

- `modifiedCategory`
- `isModified = true`
- `modifiedAt = now`

하지만 `syncTransactions()`의 재분석 여부 판별은 `modifiedCategory`를 보지 않고 `aiCategory`만 본다.

즉 현재 코드 기준 해석은 아래와 같다.

- 사용자가 카테고리를 수정해도 `aiCategory` 자체는 바뀌지 않는다.
- 재분석 조건도 `modifiedCategory`가 아니라 `aiCategory == "미분류"` 여부로만 본다.
- 따라서 사용자가 수정한 거래라도 원래 `aiCategory`가 `"미분류"`면 sync 시 재분석 대상에 다시 포함될 수 있다.

## 10. 응답값 해석

응답 DTO는 아래와 같다.

```json
{
  "syncedCount": 45,
  "lastSyncTime": "2026-03-05T10:15:00"
}
```

| 필드 | 의미 |
| :--- | :--- |
| `syncedCount` | `신규 저장 거래 수 + 기존 미분석 거래 수` |
| `lastSyncTime` | 응답 생성 시각 |

중요:
- 이름만 보면 "신규 동기화 건수"처럼 보이지만 실제 계산은 `totalSyncedCount + unanalyzedTransactions.size()`다.
- 즉 기존 DB에 있었던 거래를 다시 AI 분류한 건도 `syncedCount`에 포함된다.

## 11. 예외 및 실패 처리

`analyzeAndSaveAiMetadata()`는 AI 호출 전체를 `try-catch`로 감싼다.

AI 호출 중 예외가 발생하면:
- 에러 로그를 남긴다.
- 대상 거래 각각에 대해 `"미분류"`, `0.0` confidence 메타데이터를 저장한다.

즉 AI 서버 장애가 나더라도 거래 동기화 자체는 끝까지 진행되며, 분류 결과만 fallback 값으로 남는다.

## 12. 구현상 주의할 점

### 12.1 페이지네이션 미반복

외부 거래 조회는 페이지 `1`, 사이즈 `100`으로 고정되어 있고 다음 페이지를 반복 호출하지 않는다.

영향:
- 특정 기간에 거래가 많은 계좌는 일부 거래가 누락될 수 있다.

### 12.2 전체 거래 재스캔

재분석 단계는 요청한 `startDate`, `endDate`와 무관하게 `findByUserId(userId)`로 전체 거래를 훑는다.

영향:
- 과거 오래된 미분석 거래도 이번 sync 실행 때 함께 AI 요청에 포함된다.
- 데이터가 많아질수록 sync 비용이 커질 수 있다.

### 12.3 `syncedCount` 의미가 직관적이지 않음

응답의 `syncedCount`는 "신규 insert 건수"만 의미하지 않는다.

영향:
- 프론트나 운영에서 숫자를 "새로 수집된 거래 수"로 오해할 수 있다.

### 12.4 수동 수정 카테고리와 재분석 기준 분리

사용자 수정 카테고리는 `modifiedCategory`에 저장되고, 재분석 기준은 `aiCategory`만 본다.

영향:
- 수동 수정 정책과 sync 정책이 완전히 일치하지 않을 수 있다.

## 13. 코드 위치

- 컨트롤러 진입: `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/controller/AiTransactionController.java`
- 메인 서비스 로직: `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/service/AiTransactionService.java`
- AI 호출 클라이언트: `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/service/AiServiceClient.java`
- 거래 저장소: `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/account/repository/TransactionRepository.java`
- AI 메타데이터 엔티티: `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/domain/TransactionAiMetadata.java`

## 14. 한 줄 요약

`/api/v1/ai/transactions/sync`는 "현재 사용자의 모든 연결 계좌에서 외부 거래를 최대 100건씩 가져와 DB에 신규 저장하고, 신규 거래와 기존 미분석 거래 전체를 AI 서버에 보내 카테고리 메타데이터를 채우는 API"라고 보면 된다.
