# Spending Report Front Checklist - 2026-03-27

## 1. Purpose

This document is a front-end handoff checklist for the monthly spending report flow.

The backend side is considered implementation-complete enough to hand off, with unit tests passing.

Front work should now focus on:

- connecting the monthly report API
- validating real response mapping
- checking fallback and empty/error states

## 2. Backend Status Summary

Confirmed on the backend:

- `GET /api/v1/ai/reports?yearMonth=YYYY-MM` exists
- the response DTO includes richer report fields
- no-plan requests now return a fallback-style report flow instead of failing immediately
- AI report request/response DTOs are added
- unit tests for report mapping and no-plan request handling pass

Backend still benefits from one real AI-server contract check, but code-level handoff is ready.

## 3. API To Use

- Method: `GET`
- Path: `/api/v1/ai/reports`
- Query: `yearMonth=YYYY-MM`

Example:

```txt
/api/v1/ai/reports?yearMonth=2026-03
```

## 4. Expected Response Shape

```json
{
  "status": "SUCCESS",
  "message": "소비 리포트 조회가 완료되었습니다.",
  "data": {
    "yearMonth": "2026-03",
    "sealedAmount": 300000,
    "totalBudget": 1500000,
    "totalSpent": 1943800,
    "achievementRate": 129.6,
    "spendingPersona": "소비형",
    "aiOverallFeedback": "이번 달 소비 패턴 요약",
    "categoryReports": [
      {
        "categoryName": "식비",
        "allocatedAmount": 750000,
        "spentAmount": 900000,
        "categoryRate": 46.3,
        "budgetAchievementRate": 120.0,
        "feedback": "계획 대비 초과 소비입니다."
      }
    ],
    "personalizedFeedback": [
      {
        "type": "warning",
        "categoryName": "식비",
        "message": "식비가 계획 대비 초과되었습니다."
      }
    ]
  }
}
```

## 5. Front Mapping Checklist

### 5.1 Top summary

- [ ] `data.yearMonth` shows the target month correctly
- [ ] `data.totalBudget` is rendered
- [ ] `data.totalSpent` is rendered
- [ ] `data.achievementRate` is rendered as a readable percentage
- [ ] `data.sealedAmount` is rendered if the screen includes sealed money summary

### 5.2 Persona and overall feedback

- [ ] `data.spendingPersona` is rendered without breaking when null
- [ ] `data.aiOverallFeedback` is rendered as the main summary text

### 5.3 Category report list

- [ ] `data.categoryReports` is rendered as a list
- [ ] `categoryName` is shown
- [ ] `allocatedAmount` is shown
- [ ] `spentAmount` is shown
- [ ] `categoryRate` is shown
- [ ] `budgetAchievementRate` is shown
- [ ] `feedback` is shown

### 5.4 Personalized feedback

- [ ] `data.personalizedFeedback` is rendered as a separate list or card group
- [ ] `type` is mapped to a readable badge/label
- [ ] `categoryName` handles `null` safely
- [ ] `message` is rendered

## 6. Behavior Checklist

- [ ] loading state appears before the response arrives
- [ ] API failure shows an error state
- [ ] empty arrays do not break layout
- [ ] null persona or null category name do not break layout
- [ ] numeric fields render safely when `0`
- [ ] the screen still works when there is no spending plan for that month

## 7. No-Plan Fallback Check

This is important.

Backend now tries to generate a report even when the user has no saved spending plan.

Front should confirm:

- [ ] the monthly report screen still opens for a no-plan user
- [ ] the API does not immediately fail just because no plan exists
- [ ] category items with `allocatedAmount = 0` still render naturally
- [ ] fallback data does not require plan-only UI assumptions

## 8. Sorting Check

Backend sorts `categoryReports` using this rule:

1. `spentAmount desc`
2. tie-breaker: `categoryName asc`

Front should:

- [ ] avoid re-sorting differently unless there is a strong product reason
- [ ] confirm the displayed order matches the backend contract

## 9. Recommended QA Order

1. Connect the API and confirm one successful render with real data.
2. Confirm all top-level fields map correctly.
3. Confirm category report list rendering.
4. Confirm personalized feedback rendering.
5. Confirm no-plan fallback behavior.
6. Confirm loading, error, and empty states.
7. Capture one real response example for final contract validation.

## 10. Out Of Scope For This Handoff

These should not block monthly report front handoff:

- yearly report API work
- yearly report UI
- unrelated burndown issues
- unrelated signup/login device issues

## 11. Suggested Front Completion Definition

Front can consider the monthly report integration done when:

- the real monthly report API is connected
- all fields above render correctly
- no-plan fallback is confirmed
- loading/error/empty states are checked
- one real-device or real-server response is verified
