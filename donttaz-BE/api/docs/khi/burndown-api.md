# Burndown API Notes

## 1. Endpoint

- Path: `GET /api/v1/control/burndown`
- Query parameter: `yearMonth=YYYY-MM`
- Response type: burndown chart data for the requested month

## 2. Current Backend Behavior

### 2.1 Request handling

- Controller: `AiTransactionController#getBurndownChart`
- If `yearMonth` is missing or blank, the backend uses the current month:
  - `YearMonth.now().toString()`

### 2.2 Plan lookup

- Service: `AiTransactionService#getBurndownChart`
- The backend first looks up `spending_plan` by:
  - `userId`
  - `targetYearMonth`

### 2.3 Local/dev fallback

- If no spending plan exists, the backend calls `createLocalDummyBurndownPlan(...)`
- This fallback only works in `local` or `dev` profiles
- In that fallback, the following values are hard-coded:
  - `monthlyIncome = 3000000`
  - `totalBudget = 2500000`
  - `surplusAmount = 500000`
- In non-local/non-dev environments, missing plan data causes `INVALID_REQUEST`

### 2.4 Transaction source used for the chart

- The backend loads monthly transactions using `transactionRepository.searchWithAiMetadata(...)`
- Query range:
  - start: first day of month at `00:00`
  - end: first day of next month at `00:00`

### 2.5 Current burn-down calculation

- `dailySpentMap` is built by summing transaction `amount` by date
- Current logic does not explicitly filter only spending transactions
- As implemented now, monthly transactions are aggregated as-is
- This means deposits or non-consumption withdrawals can distort the chart

### 2.6 Ideal line calculation

- `idealRemaining` is calculated as a linear daily decrease:
  - `totalBudget - (totalBudget / daysInMonth * dayIndex)`
- This is a simple straight-line model
- Salary day, fixed expenses, and category-level budget timing are not reflected

### 2.7 Actual line calculation

- `actualRemaining = totalBudget - accumulatedSpent`
- For the current month:
  - future dates return `actualRemaining = null`
- For past months:
  - data is returned through the end of the month

### 2.8 Surviving flag

- `isSurviving` is derived by comparing:
  - actual remaining budget
  - ideal remaining budget
- If current remaining is greater than or equal to the ideal line, it returns `true`

## 3. Current Response Semantics

- `targetYearMonth`: month being rendered
- `totalBudget`: budget baseline used for the chart
- `currentRemaining`: latest actual remaining budget value
- `isSurviving`: whether actual remaining is above the ideal line
- `chartData[].date`: each calendar day in the month
- `chartData[].idealRemaining`: straight-line ideal remaining amount
- `chartData[].actualRemaining`:
  - actual remaining amount for dates with data
  - `null` for future dates in the current month

## 4. Known Gaps

### 4.1 Hard-coded fallback exists

- Local/dev can return a chart even when no spending plan exists
- This is helpful for development, but it hides missing data problems from the frontend

### 4.2 Default month policy may be ambiguous

- Burndown defaults to the current month
- Spending report defaults differently in another endpoint
- Frontend should not rely on implicit defaults unless the team agrees on one rule

### 4.3 Spending aggregation is too broad

- Current code does not clearly restrict burn-down to consumption spending only
- Deposits, transfers, rent, insurance, or card settlement transactions may be mixed into the line

### 4.4 Ideal line is simplified

- The ideal line is a uniform straight line
- It does not reflect:
  - payday timing
  - fixed expense timing
  - category allocation timing

## 5. Recommended Intended Behavior

### 5.1 Request rule

- Frontend should send `yearMonth` explicitly
- Do not depend on backend default month behavior

### 5.2 Plan dependency rule

- Burndown should use a real `spending_plan`
- If no plan exists, the API should return a clear failure instead of local dummy data

### 5.3 Transaction filtering rule

- Only spending-side transactions should affect burn-down
- At minimum, filter to `txType = WITHDRAWAL`
- Team decision needed on whether to include or exclude:
  - rent
  - financial/insurance payments
  - card settlement
  - transfer-like withdrawals

### 5.4 Frontend rendering rule

- `actualRemaining = null` should be treated as:
  - no actual data yet
  - not zero
- Frontend can stop the actual line or render future dates differently

## 6. Backend Fix Checklist

1. Decide whether `yearMonth` default should remain current month or be removed from practical use
2. Remove or clearly isolate `local/dev` dummy burndown fallback
3. Filter burndown transactions to valid spending transactions only
4. Decide category policy for fixed/financial withdrawals
5. Keep `actualRemaining = null` behavior for future dates and document it in frontend
6. Confirm whether the current straight-line `idealRemaining` is acceptable for MVP

## 7. Frontend Integration Notes

- Safe usage pattern:
  - create spending plan first
  - then request burndown for the same `yearMonth`
- Frontend should expect two possible states:
  - real chart data from an existing spending plan
  - failure when no plan exists, unless local/dev fallback is still enabled
