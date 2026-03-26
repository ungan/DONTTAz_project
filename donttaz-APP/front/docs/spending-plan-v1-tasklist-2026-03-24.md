# Spending Plan V1 Tasklist - 2026-03-24

## 1. Reference Order

Use these references in this order:

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`
3. `donttaz-APP/front/docs/spending-plan-v1-plan-2026-03-24.md`
4. `donttaz-APP/front/docs/spending-plan-v1-implementation-2026-03-24.md`
5. this tasklist

## 2. Goal

Deliver v1 with this split:

- `소비 플랜`: AI plan summary
- `대시보드`: burndown-based tracking

## 3. Work Checklist

### Phase 1. Data setup

- [ ] Add plan response types in `entities/plan`
- [ ] Add plan API file in `entities/plan/api`
- [ ] Add `use-spending-plan` hook
- [ ] Add helper logic for duplicate category merge
- [ ] Add helper logic for `riskScore` percent formatting
- [ ] Add helper logic for `budgetStrategy` label mapping

Done when:

- the page can receive normalized plan data without touching raw response fields directly

### Phase 2. Core plan UI

- [ ] Build top summary block
- [ ] Build pie chart block
- [ ] Build dashboard CTA block

Done when:

- the user can understand total budget, sealed amount, and category split at first glance

### Phase 3. Detail plan UI

- [ ] Build persona and risk block
- [ ] Build recommendation list block
- [ ] Build category detail list block

Done when:

- the plan screen has enough detail to explain why the budget was generated

### Phase 4. Plan page composition

- [ ] Update `pages/plan/ui/plan-page.tsx` to render the plan-first structure
- [ ] Add loading state for plan fetch
- [ ] Add error state for plan fetch
- [ ] Add empty state if plan data is missing
- [ ] Keep page composition simple and section-based

Done when:

- the `소비 플랜` screen works as a standalone summary page

### Phase 5. Dashboard connection

- [ ] Preserve existing burndown hook and widgets
- [ ] Confirm dashboard entry flow from the plan screen
- [ ] Update copy so dashboard clearly means tracking, not planning
- [ ] Verify chart and daily list still work as expected

Done when:

- the user can move naturally from plan to tracking without losing context

### Phase 6. QA and guardrails

- [ ] Check duplicate category handling with real response data
- [ ] Check `surplusAmount: 0` hidden behavior
- [ ] Check null reference month fields hidden behavior
- [ ] Check risk percent display
- [ ] Check strategy label display
- [ ] Check spacing, color, and typography against guide docs
- [ ] Check file placement against FSD structure

Done when:

- the screen matches the current API and still respects the front-end guides

## 4. Priority If Time Gets Tight

### Must have

- types and hook
- summary block
- pie chart block
- plan page composition
- dashboard connection

### Good to have

- persona/risk block
- category detail list

### Polish later

- recommendation styling refinement
- extra empty-state polish
- animation polish

## 5. Review Questions

Before merging or considering the work complete, ask:

- does this still follow `FRONTEND_GUIDE.md`
- does this still follow `design-guide.md`
- did we avoid mixing plan and dashboard responsibilities into one confusing screen
- did we keep `pages` focused on composition
- did we move reusable UI into `widgets` only when it was actually justified

## 6. Suggested Working Rhythm

Use this order while coding:

1. finish Phase 1
2. finish Phase 2
3. connect Phase 4 early
4. then add Phase 3 details
5. finish with Phase 5 and Phase 6

Reason:

- this gets a visible usable screen on device as early as possible
