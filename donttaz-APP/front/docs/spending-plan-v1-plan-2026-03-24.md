# Spending Plan V1 Plan - 2026-03-24

## 1. Goal



This document defines the v1 direction for the `소비 플랜` and `대시보드` tabs.

Because the remaining schedule is limited, the goal is:

- build a usable first version with the current API response
- avoid large API changes for now
- separate `계획` and `실행 추적` clearly
- leave room for later enhancement after the first implementation lands

## 1.1 Reference Priority

This document should be used with the following priority:

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`
3. this document

If this document conflicts with the guide documents:

- follow `FRONTEND_GUIDE.md` first
- follow `design-guide.md` second
- then adjust this plan document

## 1.2 Guide Compliance Check

This document was checked against the two guide files above.

It currently follows these points:

- keeps the `app / pages / features / shared` responsibility split in mind
- treats `pages` as screen composition and `widgets` as reusable UI blocks
- avoids a large speculative refactor
- keeps the page structure simple and section-based
- assumes style separation instead of inline-heavy JSX styling

This should be rechecked once real UI implementation starts.

## 2. Product Direction

### 2.1 소비 플랜

Purpose:

- show the AI-generated spending plan for the current month
- help the user understand `how much to spend`, `where to spend`, and `what to watch`

One-line meaning:

- `소비 플랜 = 이번 달 예산 계획`

### 2.2 대시보드

Purpose:

- show whether the current spending is following the plan in real time
- help the user monitor pace, remaining budget, and risk

One-line meaning:

- `대시보드 = 계획 실행 추적`

## 3. User Flow

The intended user understanding is:

1. The user opens `소비 플랜`
2. The user checks the recommended monthly plan
3. The user moves to `대시보드`
4. The user checks whether the plan is being followed

This creates a simple structure:

- first tab: `이번 달은 이렇게 써보세요`
- second tab: `지금 계획대로 가고 있나요?`

## 4. V1 Scope

### 4.1 소비 플랜 tab

This tab should focus on plan summary only.

Recommended sections:

1. Top summary card
2. Pie chart for category budget share
3. Spending persona and risk card
4. Personalized recommendation list
5. Category detail list
6. Link or CTA to dashboard

### 4.2 대시보드 tab

This tab should reuse the current burndown-driven tracking flow.

Recommended sections:

1. Current status banner
2. Burndown chart
3. Remaining budget summary
4. Daily list toggle if needed

## 5. Current API-Based Design

Current plan response example:

```json
{
  "status": "SUCCESS",
  "message": "플랜 생성 완료",
  "data": {
    "targetYearMonth": "2026-03",
    "referenceStartMonth": null,
    "referenceEndMonth": null,
    "totalBudget": 3967809,
    "sealedAmount": 750000,
    "surplusAmount": 0,
    "paydayDay": 24,
    "riskScore": 0.36,
    "budgetStrategy": "safe",
    "categoryPlans": [
      {
        "categoryName": "식비",
        "allocatedAmount": 1142045,
        "aiComment": "식비 예산을 배정했습니다."
      }
    ],
    "personalizedRecommendations": [
      {
        "type": "warning",
        "categoryName": "교통",
        "message": "교통비가 권장 비중을 초과했습니다."
      }
    ],
    "spendingPersona": "균형소비형"
  }
}
```

## 6. Field Mapping For V1

### 6.1 Top summary card

Use:

- `targetYearMonth`
- `totalBudget`
- `sealedAmount`
- `paydayDay`
- `budgetStrategy`

Suggested UI meaning:

- `이번 달 AI 소비 플랜`
- `권장 소비 예산`
- `금고 격리 금액`
- `월급일 기준`
- `안정형 전략` like label from `budgetStrategy`

### 6.2 Pie chart

Use:

- `categoryPlans[].categoryName`
- `categoryPlans[].allocatedAmount`

Rule:

- if the same category name appears more than once, merge it before rendering the chart

### 6.3 Persona and risk

Use:

- `spendingPersona`
- `riskScore`

Suggested display:

- `균형소비형`
- `위험도 36%`

Suggested risk copy:

- `0.00 ~ 0.30`: 안정적
- `0.31 ~ 0.60`: 주의 필요
- `0.61 ~ 1.00`: 지출 관리 필요

### 6.4 Recommendation list

Use:

- `personalizedRecommendations[].type`
- `personalizedRecommendations[].categoryName`
- `personalizedRecommendations[].message`

Suggested type styling:

- `warning`: caution color
- `positive`: positive color
- `habit_change`: neutral highlight color

### 6.5 Category detail list

Use:

- `categoryPlans[].categoryName`
- `categoryPlans[].allocatedAmount`
- `categoryPlans[].aiComment`

## 7. Fields To Show Later Or Hide In V1

### Show later

- `referenceStartMonth`
- `referenceEndMonth`

Reason:

- they are currently `null`
- they do not help the user much in the first version

### Hide conditionally

- `surplusAmount`

Reason:

- current example returns `0`
- `sealedAmount` already carries clearer user value

## 8. Recommended Copy

### 소비 플랜 tab

Title:

- `이번 달 AI 소비 플랜`

Description:

- `최근 소비 패턴을 바탕으로 이번 달 예산안을 만들었어요.`

Dashboard CTA:

- `대시보드에서 실시간으로 확인하기`

### 대시보드 tab

Title:

- `계획 대비 소비 현황`

Description:

- `이번 달 소비가 계획대로 진행되고 있는지 확인해보세요.`

## 9. Important Implementation Notes

### 9.1 Category duplication

Current example contains duplicated `기타`.

Decision for v1:

- merge same-name categories for chart and summary display

### 9.2 budgetStrategy label mapping

Front should map API enum to readable Korean labels.

Suggested mapping:

- `safe` -> `안정형`
- `balanced` -> `균형형`
- `aggressive` -> `공격형`

If backend values differ later, only this mapping needs to be adjusted.

### 9.3 riskScore formatting

Assume `riskScore` is a `0 ~ 1` value.

Display rule:

- multiply by 100 and round for UI

### 9.4 Sealed amount explanation

Use `sealedAmount` as:

- `금고로 분리해두면 좋은 금액`

This should be validated with product wording, but it is sufficient for v1.

## 10. Why This Direction Fits The Schedule

This plan is recommended because:

- the current API already supports it
- the current burndown flow can stay in the dashboard
- the roles of the two tabs become clear without a large redesign
- it reduces backend dependency for the first version

## 11. When To Use This Document

### Start using it now

Use this document immediately when:

- the team agrees on `소비 플랜 = 계획`, `대시보드 = 추적`
- the first UI implementation is about to begin

This is the best timing to lock v1 scope.

### Update it once after first implementation

Revise this document after:

- the first screen is implemented
- real backend data is tested on device
- any field mismatch or wording issue is found

That update can become the basis for v1.1 or polishing work.

### Do not wait too long

If the team waits until after several UI changes are already in progress, the document will become a delayed summary instead of a working reference.

Recommended timing:

- create and use this doc before coding the new plan tab
- revise once right after the first integrated screen is visible

## 13. Review Request

When this document is used for implementation or review, explicitly check:

- whether the structure still respects `FRONTEND_GUIDE.md`
- whether the visual and layout choices still respect `design-guide.md`
- whether any new widget or file split is solving a real reuse need instead of adding premature complexity

## 12. Practical Recommendation

For the current schedule, use this document in two phases:

1. Right now: as the v1 implementation guide
2. After the first integrated build: as the review checklist for polishing

That is the safest way to move quickly without losing alignment.
