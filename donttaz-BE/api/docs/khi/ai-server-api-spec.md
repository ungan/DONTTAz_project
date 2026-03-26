# AI Server API Spec

This document defines the actual request/response contract between the Spring Boot API server and the AI server.

## Scope

- This file is the source of truth for direct AI server endpoints such as `/internal/ai/categories/classify` and `/internal/ai/plans/generate`.
- `ai_api.md` documents the public backend APIs exposed by the Spring Boot server to clients.
- As a result, fields such as `transactionId` that appear in `ai_api.md` are not enough to infer the request body for the AI server classify endpoint.

## 1. Transaction Category Classification

- Endpoint: `POST {AI_SERVER_URL}/internal/ai/categories/classify`
- Method: `POST`
- Purpose: classify individual transactions into spending categories

### Request

The currently verified request shape from AI Swagger is:

```json
{
  "transactions": [
    {
      "transactionDate": "2026-03-01",
      "amount": 15000,
      "counterpartyName": "스타벅스",
      "type": "WITHDRAWAL"
    },
    {
      "transactionDate": "2026-03-02",
      "amount": 55000,
      "counterpartyName": "쿠팡",
      "type": "WITHDRAWAL"
    }
  ]
}
```

The backend may also include `transactionId` for response-to-DB mapping, as long as the AI server preserves or safely ignores it.

### Request Fields

| Field | Type | Description |
| :--- | :--- | :--- |
| `transactions` | Array | classification target list |
| `transactions[].transactionId` | String | optional backend-side identifier for DB mapping |
| `transactions[].transactionDate` | String | transaction date |
| `transactions[].amount` | Integer/Long | transaction amount |
| `transactions[].counterpartyName` | String | merchant or counterparty name |
| `transactions[].type` | String | transaction direction/type such as `WITHDRAWAL` |

### Response

The currently verified response shape from AI Swagger / live logs is:

```json
{
  "categories": [
    {
      "transactionId": 1,
      "merchantName": "스타벅스",
      "amount": 15000,
      "category": "식비",
      "confidenceScore": 0.98,
      "matchedKeyword": "스타벅스",
      "matchType": "rule_exact",
      "isModified": false
    }
  ]
}
```

### Response Fields

| Field | Type | Description |
| :--- | :--- | :--- |
| `categories` | Array | classification results |
| `categories[].transactionId` | Number or String | response-side item identifier; do not assume it is the DB primary key unless the AI server explicitly guarantees passthrough |
| `categories[].merchantName` | String | merchant name used in classification |
| `categories[].amount` | Integer/Long | classified amount |
| `categories[].category` | String | predicted category |
| `categories[].confidenceScore` | Double | model/rule confidence |
| `categories[].matchedKeyword` | String or null | matched keyword when rule-based matching succeeds |
| `categories[].matchType` | String | matching mode such as `rule_exact` or `ssafy_category` |
| `categories[].isModified` | Boolean | AI-side modification flag |

### Important Notes

- Direct AI Swagger tests succeeded with `transactionDate`, `amount`, `counterpartyName`, and `type`.
- Earlier backend requests used `transactionId`, `category`, and `description` instead, which is a different payload shape.
- Therefore, classify request format must be derived from AI Swagger / AI server contract, not from `ai_api.md`.

## 2. Spending Plan Generation

- Endpoint: `POST {AI_SERVER_URL}/internal/ai/plans/generate`
- Method: `POST`
- Purpose: generate monthly spending plan recommendations

### Request

```json
{
  "targetYearMonth": "2026-03",
  "monthlyIncome": 3000000,
  "currentBalance": 1500000,
  "history": [
    {
      "category": "식비",
      "totalAmount": 650000
    },
    {
      "category": "생활",
      "totalAmount": 200000
    },
    {
      "category": "미분류",
      "totalAmount": 50000
    }
  ]
}
```

### Request Fields

| Field | Type | Description | Notes |
| :--- | :--- | :--- | :--- |
| `targetYearMonth` | String | target year-month | `YYYY-MM` |
| `monthlyIncome` | Integer | user's monthly income | |
| `currentBalance` | Long | total account balance | |
| `history` | List | recent category summary list | |
| `history[].category` | String | category name | |
| `history[].totalAmount` | Long | summed amount for the category | |

### Response

```json
{
  "status": "SUCCESS",
  "message": "플랜 생성 완료",
  "data": {
    "targetYearMonth": "2026-03",
    "totalBudget": 2450000,
    "surplusAmount": 550000,
    "spendingPersona": "계획적인 소비형",
    "categoryPlans": [
      {
        "categoryName": "식비",
        "recommendedAmount": 600000,
        "comment": "지난달보다 5만원 줄여보는 건 어떨까요?"
      },
      {
        "categoryName": "쇼핑",
        "recommendedAmount": 300000,
        "comment": "평균적인 소비 수준입니다."
      }
    ]
  }
}
```

### Response Fields

| Field | Type | Description | Notes |
| :--- | :--- | :--- | :--- |
| `status` | String | processing status | `SUCCESS`, `FAIL` |
| `message` | String | result message | |
| `data` | Object | plan payload | |
| `data.targetYearMonth` | String | target year-month | |
| `data.totalBudget` | Integer | recommended total budget | |
| `data.surplusAmount` | Integer | remaining amount after budget | |
| `data.spendingPersona` | String | inferred spending persona | |
| `data.categoryPlans` | List | category-level plan list | |
| `data.categoryPlans[].categoryName` | String | category name | |
| `data.categoryPlans[].recommendedAmount` | Integer | recommended category budget | |
| `data.categoryPlans[].comment` | String | AI guidance comment | |

## 3. Documentation Rules

- Use `ai_api.md` for client-facing backend endpoints only.
- Use this file for backend-to-AI-server integration contracts.
- When AI Swagger and backend code disagree, AI Swagger and the live AI server response should be treated as the primary contract until code is updated.
