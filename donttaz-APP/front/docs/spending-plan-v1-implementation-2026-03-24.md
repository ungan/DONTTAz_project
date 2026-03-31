# Spending Plan V1 Implementation - 2026-03-24

## 1. Purpose

This document turns the v1 product direction into a practical front-end implementation plan.

It is based on the current structure:

- `entities/plan` currently contains burndown-only logic
- `pages/plan` currently renders the burndown page directly
- the new `소비 플랜` tab should be added without breaking the existing burndown tracking flow

## 1.1 Reference Priority

Implementation should follow these references in this order:

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`
3. this implementation document

If there is any mismatch:

- use `FRONTEND_GUIDE.md` as the first decision source
- use `design-guide.md` as the second decision source
- then revise this document if needed

## 1.2 Current Compliance Check

This implementation plan was reviewed against the two guide documents above.

It is currently aligned in these areas:

- keeps routing and screen composition centered in `app` and `pages`
- places API and model logic in `entities`
- uses `widgets` for reusable screen blocks instead of overloading the page file
- avoids broad redesign beyond the current schedule needs
- keeps UI assembly simple and section-oriented

Implementation should still be checked again during code review.

## 2. Implementation Goal

The target structure for v1 is:

- `소비 플랜` tab: plan summary screen
- `대시보드` tab: burndown tracking screen

The main principle is:

- do not redesign the entire page architecture
- reuse the existing burndown logic as much as possible
- add only the minimum new layers needed for plan rendering

## 3. Recommended Build Order

### Step 1. Lock the page role split

Confirm and keep this rule:

- `소비 플랜` shows AI plan summary
- `대시보드` shows tracking and burndown

Why first:

- this avoids mixing plan and tracking UI in one screen
- this lets the current burndown code survive with minimal changes

### Step 2. Add plan entity types and API hook

Before any heavy UI work, define the data shape for the new plan response.

Why second:

- the screen depends on stable plan field names
- once the entity layer exists, UI work becomes straightforward

### Step 3. Build the plan widgets

Build the sections in this order:

1. top summary card
2. pie chart
3. persona/risk card
4. recommendation list
5. category detail list
6. dashboard CTA

Why this order:

- the first three blocks already provide a usable screen
- the later blocks add depth without blocking the first render

### Step 4. Keep the dashboard on existing burndown flow

Do not rewrite burndown logic during the first pass.

For v1, the safer choice is:

- keep `useBurndown`
- keep `BurndownChart`
- keep `BurndownDailyList`
- move or relabel the screen as dashboard-oriented if needed

### Step 5. Polish and verify with real backend data

After the first screen is visible:

- test category duplication handling
- test risk formatting
- test empty state
- test loading and error state

## 4. Recommended Front Structure

### 4.1 Entity layer

The current `entities/plan` folder should grow from burndown-only into a plan domain folder.

Recommended additions:

- `entities/plan/api/create-spending-plan.ts`
- `entities/plan/model/use-spending-plan.ts`
- `entities/plan/model/plan-types.ts`

Suggested responsibility:

- API call
- response parsing
- plan-related types
- small plan-focused data normalization helpers

Examples of helpers that fit here:

- merging duplicate category names
- converting `riskScore` into UI-friendly percent
- mapping `budgetStrategy` to display label

### 4.2 Widget layer

Add new widgets for the plan summary UI.

Recommended widgets:

- `widgets/spending-plan-summary`
- `widgets/spending-plan-pie-chart`
- `widgets/spending-plan-persona`
- `widgets/spending-plan-recommendations`
- `widgets/spending-plan-category-list`

Suggested principle:

- each widget should render one meaningful screen block
- keep transformation logic out of the widget when possible

### 4.3 Page layer

The page should stay mostly compositional.

Recommended direction:

- `pages/plan/ui/plan-page.tsx` becomes the plan-summary page
- existing burndown rendering moves into a dashboard-oriented page if the route structure allows it

If route changes are too expensive for the schedule:

- keep the existing route
- introduce an internal tab switch or selector
- but still preserve the conceptual split between `plan` and `dashboard`

## 5. Suggested File Plan

This is the lowest-risk file split for the current repository.

### Plan entity files

- `front/entities/plan/model/plan-types.ts`
- `front/entities/plan/model/use-spending-plan.ts`
- `front/entities/plan/api/create-spending-plan.ts`

### New plan widgets

- `front/widgets/spending-plan-summary/ui/spending-plan-summary.tsx`
- `front/widgets/spending-plan-summary/ui/spending-plan-summary.styles.ts`
- `front/widgets/spending-plan-pie-chart/ui/spending-plan-pie-chart.tsx`
- `front/widgets/spending-plan-pie-chart/ui/spending-plan-pie-chart.styles.ts`
- `front/widgets/spending-plan-persona/ui/spending-plan-persona.tsx`
- `front/widgets/spending-plan-recommendations/ui/spending-plan-recommendations.tsx`
- `front/widgets/spending-plan-category-list/ui/spending-plan-category-list.tsx`

### Existing burndown files to preserve

- `front/entities/plan/api/get-burndown.ts`
- `front/entities/plan/model/use-burndown.ts`
- `front/widgets/burndown-chart/ui/burndown-chart.tsx`
- `front/widgets/burndown-daily-list/ui/burndown-daily-list.tsx`

## 6. UI Priority

If time becomes tight, implement in this order.

### Priority 1

- summary card
- pie chart
- dashboard CTA

Reason:

- this already communicates the plan clearly

### Priority 2

- persona/risk card
- category list

Reason:

- adds useful detail without major complexity

### Priority 3

- recommendation list styling polish
- empty state refinement
- animation

Reason:

- good for finish quality, but not required for first delivery

## 7. Data Handling Rules

### 7.1 Duplicate categories

Current sample includes duplicated `기타`.

V1 rule:

- merge duplicated names before feeding chart or list

Suggested merged shape:

- same `categoryName`
- sum `allocatedAmount`
- preserve one representative `aiComment` or join comments only if necessary

### 7.2 Risk score

V1 rule:

- assume `riskScore` is between `0` and `1`
- convert to integer percentage for display

### 7.3 Strategy label

V1 rule:

- front maps API enum values to Korean labels

Suggested mapping:

- `safe` -> `안정형`
- `balanced` -> `균형형`
- `aggressive` -> `집중형`

### 7.4 Optional fields

V1 rule:

- hide `referenceStartMonth` and `referenceEndMonth` if null
- hide `surplusAmount` if `0`

## 8. Screen Assembly Recommendation

The first visible version of the plan page should be assembled like this:

1. `PageHeader`
2. plan summary card
3. pie chart block
4. persona/risk card
5. recommendation list
6. category list
7. CTA to dashboard

This order matches the user reading flow:

- overall plan
- budget distribution
- user type and risk
- suggested actions
- detail breakdown
- move to tracking

## 9. Recommended CTA Direction

The plan page should always give the user a clear next action.

Suggested CTA copy:

- `대시보드에서 실시간으로 확인하기`

This is important because the two screens only make sense as a pair:

- plan without tracking feels incomplete
- tracking without plan feels contextless

## 10. Risks To Avoid

### Avoid mixing both purposes into one long page

If the plan summary and burndown chart are stacked in one screen immediately:

- the screen will feel crowded
- the role of each section becomes unclear
- the schedule may slip due to extra layout work

### Avoid over-expanding the API in v1

If the team waits for more fields before implementation:

- front work may stall
- current usable data goes unused

### Avoid deep refactor of the burndown flow

The burndown screen is already working.

For v1:

- preserve it
- relabel or reposition it if needed
- do not rewrite it unless a bug forces the change

## 11. Definition Of Done For V1

The v1 implementation is good enough when:

- the plan page renders current API data successfully
- category budget is visible in a pie chart
- the user can read total budget and sealed amount quickly
- recommendations and category comments are visible
- the user can move naturally to the dashboard
- the dashboard still renders burndown data correctly

## 12. When To Use This Document

Use this document:

- right before implementation starts
- during implementation as the task checklist
- once again during the first integrated QA pass

Best timing:

- now, before the new plan UI files are created

Update timing:

- right after the first end-to-end screen is visible with real or mock backend data

## 13. Required Review Check

During implementation review, verify these items explicitly:

- does the file placement still match `FRONTEND_GUIDE.md`
- does the screen remain visually consistent with `design-guide.md`
- are shared or reusable elements really placed outside the page only when reuse is justified
- are styles separated cleanly instead of being scattered inline
- did any shortcut taken for schedule reasons accidentally break the project guide rules
