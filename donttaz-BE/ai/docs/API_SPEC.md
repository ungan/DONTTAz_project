# AI 서버 API 명세

> Base URL (Spring 내부 호출): `http://donttaz-ai:8000/internal/ai`
> Content-Type: `application/json`

---

## 공통 거래 객체 (`AiTransaction`)

플랜, 리포트, 번다운 요청에 공통으로 사용되는 거래 객체입니다.

```json
{
  "id": "거래ID (선택)",
  "transactionDate": "2025-03-15",
  "amount": 35000,
  "counterpartyName": "스타벅스",
  "category": "식비",
  "memo": "아메리카노",
  "type": "WITHDRAWAL"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | string | 선택 | 거래 고유 ID |
| `transactionDate` | string | **필수** | 거래일 `YYYY-MM-DD` |
| `amount` | int | **필수** | 금액 (원 단위 양수) |
| `counterpartyName` | string | 선택 | 가맹점명 / 거래처명 |
| `category` | string | 선택 | 카테고리 (없으면 AI가 분류) |
| `memo` | string | 선택 | 메모 |
| `type` | string | 선택 | `WITHDRAWAL` (지출) / `DEPOSIT` (입금) |

> 계좌 거래의 경우 `type`을 반드시 전달해야 입금/지출 구분이 가능합니다.

---

## 1. 플랜 생성

### `POST /plans/generate`

다음달 예산 플랜을 생성합니다.

#### Request Body

```json
{
  "targetYearMonth": "2025-04",
  "currentBalance": 800000,
  "paydayDay": 25,
  "cardTransactions": [ /* AiTransaction 배열, 최근 2~3개월 */ ],
  "accountTransactions": [ /* AiTransaction 배열, 최근 2~3개월 */ ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `targetYearMonth` | string | **필수** | 플랜 대상 년월 `YYYY-MM` |
| `currentBalance` | int | **필수** | 현재 잔액 |
| `paydayDay` | int | 선택 | 월급일 (기본값 25) |
| `cardTransactions` | array | 선택 | 카드 거래 내역 |
| `accountTransactions` | array | 선택 | 계좌 거래 내역 |

> 월 소득과 고정지출은 AI가 계좌 거래 내역에서 자동으로 추정합니다. Spring에서 별도로 전달하지 않아도 됩니다.

#### Response Body

```json
{
  "status": "SUCCESS",
  "message": "플랜 생성 완료",
  "data": {
    "targetYearMonth": "2025-04",
    "referenceStartMonth": null,
    "referenceEndMonth": null,
    "totalBudget": 620000,
    "sealedAmount": 180000,
    "surplusAmount": 0,
    "paydayDay": 25,
    "riskScore": 0.62,
    "budgetStrategy": "balanced",
    "spendingPersona": "식도락형",
    "categoryPlans": [
      {
        "categoryName": "식비",
        "allocatedAmount": 250000,
        "aiComment": "지난달 대비 식비가 15% 증가했습니다. 배달 빈도를 줄여보세요."
      }
    ],
    "personalizedRecommendations": [
      {
        "type": "warning",
        "categoryName": "식비",
        "message": "이번달 식비 지출이 권장 비중을 초과했습니다."
      },
      {
        "type": "habit_change",
        "categoryName": null,
        "message": "이번달 소비 성향이 생활밀착형에서 식도락형으로 바뀌었어요."
      }
    ]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `totalBudget` | int | 사용 가능 예산 |
| `sealedAmount` | int | 봉인금액 (건드리지 말아야 할 예비비) |
| `surplusAmount` | int | 잉여금 |
| `riskScore` | float | 소비 위험도 0.0~1.0. 예측지출 비율(0.5) + 카테고리 압박(0.3) + 번다운 이탈(0.2) 가중합 |
| `budgetStrategy` | string | `safe` / `balanced` / `tight` |
| `spendingPersona` | string\|null | 소비 페르소나 |
| `categoryPlans[].categoryName` | string | 카테고리명 |
| `categoryPlans[].allocatedAmount` | int | 배정 금액 |
| `categoryPlans[].aiComment` | string | AI 코멘트 |
| `personalizedRecommendations[].type` | string | `warning` / `action` / `habit_change` / `saving_tip` / `positive` |
| `personalizedRecommendations[].categoryName` | string\|null | 관련 카테고리 |
| `personalizedRecommendations[].message` | string | 추천 메시지 |

---

## 2. 리포트 생성

### `POST /reports/generate`

월 소비 분석 리포트를 생성합니다.

#### Request Body

```json
{
  "yearMonth": "2025-03",
  "paydayDay": 25,
  "plan": {
    "totalBudget": 620000,
    "sealedAmount": 180000,
    "categoryPlans": [
      {
        "categoryName": "식비",
        "allocatedAmount": 250000,
        "aiComment": ""
      }
    ]
  },
  "cardTransactions": [ /* AiTransaction 배열, 해당 월 */ ],
  "accountTransactions": [ /* AiTransaction 배열, 해당 월 */ ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `yearMonth` | string | **필수** | 리포트 대상 년월 `YYYY-MM` |
| `paydayDay` | int | 선택 | 월급일 (신규 가입자처럼 플랜이 없는 경우 소득 추정에 사용) |
| `plan` | object | 선택 | 해당 월 플랜 정보 (없으면 AI가 예산 추정 — 신규 가입자 케이스) |
| `plan.totalBudget` | int | - | 총 예산 |
| `plan.sealedAmount` | int | - | 봉인금액 |
| `plan.categoryPlans` | array | - | 카테고리별 예산 |
| `cardTransactions` | array | 선택 | 카드 거래 내역 |
| `accountTransactions` | array | 선택 | 계좌 거래 내역 |

> `plan`이 있는 경우 `paydayDay`는 사용되지 않습니다.

#### Response Body

```json
{
  "status": "SUCCESS",
  "message": "분석 리포트 생성 완료",
  "data": {
    "yearMonth": "2025-03",
    "sealedAmount": 180000,
    "totalBudget": 620000,
    "totalSpent": 540000,
    "achievementRate": 87.1,
    "spendingPersona": "식도락형",
    "aiOverallFeedback": "전체적으로 예산 범위 내에서 소비를 유지했습니다. 식비 지출이 다소 높으니 다음달에는 조정을 추천합니다.",
    "categoryReports": [
      {
        "categoryName": "식비",
        "allocatedAmount": 250000,
        "spentAmount": 280000,
        "categoryRate": 51.8,
        "feedback": "계획 대비 초과 소비입니다."
      }
    ],
    "personalizedFeedback": [
      {
        "type": "warning",
        "categoryName": "식비",
        "message": "식비가 계획 대비 30,000원 초과되었습니다."
      },
      {
        "type": "positive",
        "categoryName": "교통",
        "message": "교통는 계획 범위 내에서 안정적으로 관리되었습니다."
      },
      {
        "type": "pattern",
        "categoryName": null,
        "message": "야간 소비 비중이 높았습니다. 늦은 시간 결제를 줄이면 변동지출 관리에 도움이 됩니다."
      }
    ]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `totalSpent` | int | 해당 월 총 지출 |
| `achievementRate` | float | 예산 달성률 (%) |
| `spendingPersona` | string\|null | 소비 페르소나 (`식도락형` / `쇼핑족` / `생활밀착형` / `여가충전형` / `자기계발형` / `재무관리형` / `균형소비형`) |
| `aiOverallFeedback` | string | **LLM** 생성 전체 종합 피드백 한 단락 (페르소나 포함) |
| `categoryReports[].categoryRate` | float | 전체 지출 중 해당 카테고리 비중 (%) |
| `categoryReports[].feedback` | string | 룰 기반 예산 대비 피드백 ("계획 대비 초과 소비입니다." 등) |
| `personalizedFeedback[].type` | string | `warning` / `positive` / `pattern` |
| `personalizedFeedback[].message` | string | 룰 기반 생성 (예산 초과 금액, 야간 소비 비율 등 조건 기반) |

> **LLM 사용 필드 요약**
> - 플랜: `categoryPlans[].aiComment`, `personalizedRecommendations`
> - 리포트: `aiOverallFeedback`
>
> 나머지 피드백(`personalizedFeedback`, `categoryReports[].feedback`)은 모두 룰 기반으로 생성됩니다.

---

## 3. 번다운 차트

### `POST /burndown/calculate`

현재 시점 기준 급여 주기(last payday ~ next payday)의 잔액 소진 차트 데이터를 반환합니다.

> `yearMonth`는 응답에 그대로 포함되는 표시용 필드이며, 실제 차트 범위 계산은 **현재 날짜 + 월급일** 기준으로 수행됩니다.
> 또한 이 API는 다른 AI API와 달리 `status/message/data` 래핑 없이 결과 객체를 직접 반환합니다.

#### Request Body

```json
{
  "yearMonth": "2025-03",
  "currentBalance": 800000,
  "plan": {
    "totalBudget": 620000,
    "sealedAmount": 180000,
    "paydayDay": 25
  },
  "cardTransactions": [ /* AiTransaction 배열, 최근 1~2개월 */ ]
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `yearMonth` | string | **필수** | 대상 년월 `YYYY-MM` |
| `currentBalance` | int | **필수** | 현재 잔액 |
| `plan` | object | 선택 | 플랜 정보 (없으면 AI가 봉인금액 자동 산정) |
| `plan.totalBudget` | int | - | 총 예산 |
| `plan.sealedAmount` | int | - | 봉인금액 (기본값 0) |
| `plan.paydayDay` | int | - | 월급일 (기본값 25) |
| `cardTransactions` | array | 선택 | 카드 거래 내역 |

#### Response Body

```json
{
  "yearMonth": "2025-03",
  "currentBalance": 800000,
  "sealedAmount": 180000,
  "usableBalance": 620000,
  "predictedTotalSpending": 580000,
  "zeroDate": null,
  "daysToZero": null,
  "isSurviving": true,
  "chartData": [
    {
      "date": "2025-02-25",
      "idealRemaining": 620000,
      "actualRemaining": 620000,
      "predictedRemaining": 620000
    },
    {
      "date": "2025-03-01",
      "idealRemaining": 597000,
      "actualRemaining": 585000,
      "predictedRemaining": 585000
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `usableBalance` | int | 봉인 제외 사용 가능 잔액 |
| `predictedTotalSpending` | int | 월급일까지 예측 총 지출 |
| `zeroDate` | string\|null | 잔액 소진 예상일 (`YYYY-MM-DD`), 소진 안 되면 null |
| `daysToZero` | int\|null | 소진까지 남은 일수, 소진 안 되면 null |
| `isSurviving` | bool | 월급일까지 잔액이 버티는지 여부 |
| `chartData[].date` | string | 날짜 |
| `chartData[].idealRemaining` | int | 이상적 잔액 (선형 감소) |
| `chartData[].actualRemaining` | int | 실제 잔액 (오늘까지) |
| `chartData[].predictedRemaining` | int | 예측 잔액 (내일 이후) |

---

## 4. 카테고리 분류

### `POST /categories/classify`

거래 목록의 카테고리를 AI가 분류합니다.

#### Request Body

```json
{
  "transactions": [
    {
      "transactionId": "tx_001",
      "counterpartyName": "스타벅스",
      "amount": 5500,
      "category": null
    }
  ]
}
```

#### Response Body

```json
{
  "categories": [
    {
      "transactionId": "tx_001",
      "merchantName": "스타벅스",
      "amount": 5500,
      "category": "식비",
      "confidenceScore": 0.98,
      "matchedKeyword": "스타벅스",
      "matchType": "rule_exact",
      "isModified": false
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `category` | string | 분류된 카테고리 |
| `confidenceScore` | float | 분류 신뢰도 0.0~1.0 |
| `matchType` | string | `rule_exact` / `rule_partial` / `embedding` / `ssafy_category` / `default` |
| `isModified` | bool | 사용자 수정 여부 (항상 false, 수정은 PATCH로) |

---

### `POST /categories/classify/ssafy`

SSAFY **카드 거래** 응답 포맷을 입력받아 내부 분류 형식으로 변환한 뒤 카테고리 분류 결과를 반환합니다. 계좌 거래 등 다른 응답 포맷은 지원하지 않습니다.

#### Request Body

SSAFY 카드 거래 조회 API의 원본 JSON (`REC` 키 포함)을 그대로 전달합니다.

#### Response Body

`POST /categories/classify`와 동일한 형식으로 반환됩니다.

---

### `PATCH /categories`

사용자가 직접 카테고리를 수정할 때 호출합니다.

#### Request Body

```json
{
  "transactionId": "tx_001",
  "category": "생활"
}
```

#### Response Body

```json
{
  "transactionId": "tx_001",
  "category": "생활",
  "isModified": true,
  "message": "카테고리가 수정되었습니다."
}
```

---

## 카테고리 목록 (고정값)

```
식비 / 쇼핑 / 교통 / 생활 / 여가 / 의료/건강 / 교육 / 금융/보험 / 경조사/선물 / 기타
```

---

## 에러 처리

AI 내부 처리 실패(LLM 오류, 예측 실패 등)는 서버가 자동으로 fallback 처리하므로 별도 에러 응답 없이 항상 `200 OK`로 응답합니다.
입력값 형식 오류는 `422 Unprocessable Entity`로 응답합니다.
