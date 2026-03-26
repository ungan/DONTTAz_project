# `/api/v1/ai/transactions/reanalyze` 정리

## 목적

`/api/v1/ai/transactions/sync`의 역할은 유지한 채, DB에 이미 저장된 거래를 기간 기준으로 다시 AI 분류하기 위한 별도 API다.

- `sync`: 외부 금융 API에서 거래를 가져와 저장하고 AI 분류
- `reanalyze`: DB에 저장된 거래를 다시 AI 분류

즉 과거 거래 백필, 재분류, 강제 재분석 용도로 사용한다.

## 엔드포인트

- Method: `POST`
- Path: `/api/v1/ai/transactions/reanalyze`

## Request Body

```json
{
  "startDate": "2025-12-01",
  "endDate": "2026-03-01",
  "force": false
}
```

| 필드 | 타입 | 설명 |
| :--- | :--- | :--- |
| `startDate` | `LocalDate` | 재분류 시작일 |
| `endDate` | `LocalDate` | 재분류 종료일 |
| `force` | `Boolean` | `true`면 기존 AI 메타데이터가 있어도 다시 AI 요청을 보냄 |

기본값:
- `startDate` 미입력 시 `endDate - 3개월`
- `endDate` 미입력 시 `오늘`
- `force` 미입력 시 `false`

## 동작 방식

1. 현재 로그인 사용자의 거래를 DB에서 기간 기준으로 조회한다.
2. `force=false`면 아래 대상만 다시 분류한다.
   - AI 메타데이터가 없는 거래
   - `aiCategory`가 `미분류`인 거래
3. `force=true`면 기간 내 조회된 거래를 모두 다시 분류한다.
4. AI 결과는 `transaction_ai_metadata`에 업서트한다.
   - 기존 메타데이터가 있으면 카테고리/신뢰도만 갱신
   - 없으면 새로 생성

## Response 예시

```json
{
  "status": "SUCCESS",
  "message": "Stored transaction reanalysis completed.",
  "data": {
    "startDate": "2025-12-01",
    "endDate": "2026-03-01",
    "matchedTransactionCount": 45,
    "reanalyzedCount": 12,
    "force": false,
    "lastAnalyzedTime": "2026-03-25T14:30:00"
  }
}
```

## 사용 기준

- 외부에서 거래를 새로 가져와야 하면 `sync`
- DB에 있는 거래를 다시 분류해야 하면 `reanalyze`

현재 요구사항인 "DB에 이미 있는 과거 거래를 다시 AI로 보내기"에는 `reanalyze`가 더 적합하다.
