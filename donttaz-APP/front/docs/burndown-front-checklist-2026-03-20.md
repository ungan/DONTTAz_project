# Burndown Front Checklist - 2026-03-20

## 1. Reference Docs

- `donttaz-APP/FRONTEND_GUIDE.md`
- `donttaz-APP/front/docs/design-guide.md`
- `donttaz-APP/front/docs/bundown.md` section 4.6

This checklist is for front-end burndown work only.
When implementing or revising the burndown screen, follow the structure and UI rules from the two front-end guides above.

## 2. Current Front Structure

- Route: `app/(tabs)/plan.tsx`
- Tab registration: `app/(tabs)/_layout.tsx`
- Page: `pages/plan/ui/plan-page.tsx`
- Chart widget: `widgets/burndown-chart/ui/burndown-chart.tsx`
- Daily list widget: `widgets/burndown-daily-list/ui/burndown-daily-list.tsx`
- Data hook: `entities/plan/model/use-burndown.ts`
- API call: `entities/plan/api/get-burndown.ts`
- Types: `entities/plan/model/types.ts`
- Mock data: `shared/mock/data/burndown-surviving.json`

## 3. Confirmed Decisions

- Keep the current chart/list toggle UX.
- Keep the current `isSurviving === false` UI behavior as-is for now.
- Use the current month as the default `yearMonth`.
- Align the API request format to the backend spec in `bundown.md` section 4.6.
- For now, keep future-day handling simple and do not redesign it prematurely.

## 4. API Alignment Status

Backend spec for section 4.6:

- Endpoint: `GET /control/burndown`
- `yearMonth`: query parameter

Front status:

- Updated to send `yearMonth` through query params instead of headers.
- Existing response shape already matches the current front types closely.

Expected response fields:

- `targetYearMonth`
- `totalBudget`
- `currentRemaining`
- `isSurviving`
- `chartData[]`
  - `date`
  - `idealRemaining`
  - `actualRemaining`

## 5. Current Screen Behavior

- The plan page fetches burndown data for the current month.
- The default view is the chart.
- Pressing the calendar icon switches to the daily list view.
- Pressing the chart icon in the list switches back to the chart.
- Future dates are currently represented with `actualRemaining: null`.
- The list shows `-` when actual remaining data is not available.

## 6. Recommended Near-Term Rule For `yearMonth`

Use this rule until product requirements change:

- Default to the current month automatically.
- Keep the API and hook shape compatible with arbitrary `yearMonth` values.
- Delay month-picker UI until there is a clear requirement for cross-month browsing.

Reason:

- It keeps the first UX simple.
- It matches the current page behavior.
- It leaves room for a month selector later without reworking the data layer.

## 7. Information Still Needed From Backend/Product

These are the remaining questions that matter most before polishing the screen:

- What is the response format when no spending plan exists for the month?
- What is the response format when burndown data is empty?
- Are past months allowed?
- Are future months allowed?
- Is there a product decision for the empty state UI when plan data does not exist?

## 8. Front Work Checklist

- [x] Locate existing burndown-related screen and widgets.
- [x] Compare front structure with API spec section 4.6.
- [x] Align `yearMonth` request format to backend spec.
- [x] Keep current toggle structure and current-month default.
- [x] Prepare local/dev backend fallback so front integration is testable before AI plan flow is complete.
- [ ] Define empty-state UX for "plan not found" or "data not found".
- [ ] Decide whether month selection UI is needed.
- [ ] Decide whether future dates need a stronger visual distinction.
- [ ] Verify real backend response with at least one success case.
- [ ] Verify real backend response with at least one empty/error case.

## 9. Working Assumption For Next UI Pass

Unless new requirements arrive, the next burndown UI pass should assume:

- current month only
- chart first, list second
- existing danger/survival visual language
- no month picker yet
- no major redesign of future-date rows yet
