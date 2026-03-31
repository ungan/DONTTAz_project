# Burndown Follow-up - 2026-03-25

## 1. Purpose

이 문서는 소비 플랜/번다운 연동 과정에서 2026-03-25 기준으로 확인된 상태와,
추가 확인이 필요한 항목을 짧게 남겨두기 위한 메모다.

## 2. Current Status

### 2.1 Spending plan screen

- 소비 플랜 화면 진입 시 프론트에서 실제로 `POST /api/v1/ai/plans` 호출이 발생한다.
- 로그인 전에는 `403`이 발생했고, 로그인 후에는 `500`으로 바뀌었다.
- 현재 프론트는 `/ai/plans` 실패 시 fallback 데이터로 화면을 유지하도록 되어 있다.

### 2.2 Root cause found on 2026-03-25

백엔드 로그 기준으로 현재 실패 원인은 인증 문제가 아니라 `monthlyIncome` 값이 `null`로 AI 서버에 전달되는 점이다.

확인된 로그 요약:

- 프론트:
  - `[PlanDebug][Hook] fetch:start {"monthlyIncome": null, ...}`
  - `[PlanDebug][API] request:start {"targetYearMonth": "2026-03"}`
- 백엔드:
  - `422 Unprocessable Entity`
  - `monthlyIncome: Input should be a valid integer`

즉 현재 앱에서 `useSpendingPlan(...)` 호출 시 `monthlyIncome`를 넘기지 않고 있고,
백엔드가 이를 그대로 AI 서버에 전달하면서 실패하고 있다.

## 3. Immediate Next Actions

1. 프론트에서 소비 플랜 호출 시 `monthlyIncome` 테스트 값을 명시해서 전달
   - 예: `3000000`
2. `/api/v1/ai/plans`가 fallback이 아니라 실제 응답으로 성공하는지 확인
3. 성공 후 `/control/burndown?yearMonth=2026-03` 응답 확인
4. 마지막으로 앱에서 소비 플랜 -> 대시보드 흐름 점검

## 4. Burndown Check Point

현재 번다운 차트에서 확인하고 싶은 핵심은 아래 두 가지다.

- 실제 남은 금액 추이
- 오늘 기준 이상적으로 남아 있어야 하는 금액 추이

즉 차트에서 아래 값이 모두 존재하는지 확인이 필요하다.

- `actualRemaining`
- `idealRemaining`

## 5. Question To Ask Backend Team

```text
안녕하세요. 번다운 차트 관련해서 확인 부탁드립니다.

현재 잔액 추이(actualRemaining)뿐 아니라,
“오늘 기준 원래 어느 정도 남아 있어야 하는지”를 나타내는 ideal line도 같이 내려오는지 확인하고 싶습니다.

확인 요청드리는 항목:
1. `/control/burndown` 응답의 `chartData`에 `idealRemaining`이 실제로 포함되는지
2. `idealRemaining` 계산 기준이 무엇인지
3. `actualRemaining`과 `idealRemaining`이 각각 어떤 의미인지
4. 가능하면 `/control/burndown?yearMonth=2026-03` 실제 응답 샘플 1개

프론트에서 차트에 두 개의 선을 정확히 구분해서 보여주기 위해 확인이 필요합니다.
```

## 6. Front Checklist

- [ ] `useSpendingPlan(...)` 호출부에 `monthlyIncome` 값이 실제로 들어가는지 확인
- [ ] `/api/v1/ai/plans`가 fallback이 아니라 실제 성공 응답을 받는지 확인
- [ ] 소비 플랜 화면에 실제 응답이 반영되는지 확인
- [ ] `/control/burndown` 응답에 `idealRemaining`이 포함되는지 확인
- [ ] 번다운 차트가 `actualRemaining`과 `idealRemaining`을 모두 그리는지 확인

## 7. Reference

- `donttaz-APP/front/docs/spending-plan-integration-handoff-2026-03-24.md`
