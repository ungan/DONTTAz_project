# Burndown Integration Check - 2026-03-20

## 1. Reference Docs

- `donttaz-APP/FRONTEND_GUIDE.md`
- `donttaz-APP/front/docs/design-guide.md`
- `donttaz-APP/front/docs/bundown.md` section 4.6

This document summarizes the current burndown integration status and the next real-server verification steps.

## 2. Current Status

The front-end burndown flow is prepared for backend integration.

Completed:

- existing burndown screen location identified
- current front structure documented
- request format aligned with backend spec
- `yearMonth` now sent as a query parameter
- empty state improved
- type check passed
- local/dev backend fallback prepared so the current month can return burndown data even when a spending plan has not been created yet

Not fully verified yet:

- real backend success response
- real backend empty response
- real backend error response

## 3. Current Front Structure

- Route: `app/(tabs)/plan.tsx`
- Tab layout guard: `app/(tabs)/_layout.tsx`
- Page: `pages/plan/ui/plan-page.tsx`
- Chart widget: `widgets/burndown-chart/ui/burndown-chart.tsx`
- Daily list widget: `widgets/burndown-daily-list/ui/burndown-daily-list.tsx`
- Hook: `entities/plan/model/use-burndown.ts`
- API: `entities/plan/api/get-burndown.ts`
- Types: `entities/plan/model/types.ts`

## 4. Backend Alignment

Backend spec from `bundown.md` section 4.6:

- Method: `GET`
- Endpoint: `/control/burndown`
- Parameter: `yearMonth`
- Parameter location: query parameter

Front status:

- updated to call `/control/burndown?yearMonth=YYYY-MM`
- no longer sending `yearMonth` through request headers

## 5. Expected Response Shape

The current front expects this response structure:

```json
{
  "status": "SUCCESS",
  "message": "string",
  "data": {
    "targetYearMonth": "2026-03",
    "totalBudget": 2500000,
    "currentRemaining": 1850000,
    "isSurviving": true,
    "chartData": [
      {
        "date": "2026-03-05",
        "idealRemaining": 2096774,
        "actualRemaining": 1850000
      }
    ]
  }
}
```

## 6. Current Screen Behavior

### Success case

- the plan page loads burndown data for the current month
- the default view is the chart
- the user can toggle between chart and daily list

### Empty case

- if there is no usable burndown data, the page shows a preview burndown chart
- a dim overlay message appears on top
- the message tells the user to create a spending plan first

### Error case

- if the request fails, the page shows an error card
- the page includes a retry button

## 7. Real Server Verification Setup

Check `front/.env` first:

```env
EXPO_PUBLIC_IS_MOCK=false
EXPO_PUBLIC_BASE_URL=http://PC_IP:8080/api/v1
```

Rules:

- use the real backend address, not `localhost`
- restart Expo after editing `.env`

## 8. App Run Command

```bash
cd C:\ssafy\project2\S14P21C104\donttaz-APP\front
npx expo start --go --tunnel --clear
```

## 9. Real Server Verification Flow

1. set `EXPO_PUBLIC_IS_MOCK=false`
2. set `EXPO_PUBLIC_BASE_URL` to the real backend base URL
3. restart Expo
4. log in
5. open the `플랜` tab
6. observe which state appears

## 10. What To Check On The Plan Screen

### If integration is successful

- the burndown chart is rendered
- chart/list toggle works

### If there is no plan data

- preview chart is visible
- overlay message says the user should create a spending plan first

### If the request fails

- an error card appears
- retry button appears

## 11. Backend Checkpoint

The backend should receive a request like this:

```txt
GET /api/v1/control/burndown?yearMonth=2026-03
```

Temporary local/dev rule:

- if there is no spending plan for the requested month, local/dev currently returns dummy burndown data instead of immediately failing
- production-like environments should still keep the existing invalid-request behavior until product policy is finalized

## 12. Information Still Needed

These items are still useful for a full finish:

- actual success response example from backend
- actual empty response example from backend
- actual error response example from backend
- confirmation whether past months are supported
- confirmation whether future months are supported

## 13. Done / Remaining

Done:

- API request shape aligned
- front empty state prepared
- retry flow prepared
- type check passed

Remaining:

- verify with real backend
- confirm empty/error response contracts
- refine final UX if backend rules differ
