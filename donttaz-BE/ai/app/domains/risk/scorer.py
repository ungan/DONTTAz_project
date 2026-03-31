from calendar import monthrange
from collections import defaultdict
from datetime import datetime
from typing import Optional


def _clamp(x: float, low: float = 0.0, high: float = 1.0) -> float:
    return max(low, min(x, high))


def _parse_tx_date(value: str) -> Optional[datetime]:
    """정규화된 거래 날짜 문자열 파싱. normalization_service 출력은 YYYY-MM-DD 고정."""
    if not value:
        return None
    for fmt in ("%Y-%m-%d", "%Y-%m-%dT%H:%M:%S", "%Y%m%d"):
        try:
            return datetime.strptime(str(value)[:19], fmt)
        except ValueError:
            continue
    return None


def _get_period_bounds(today: datetime, payday_day: int):
    """현재 급여 주기의 시작일(last_payday)과 종료일(next_payday) 반환."""
    y, m, d = today.year, today.month, today.day

    def safe_payday(year, month):
        last_day = monthrange(year, month)[1]
        return datetime(year, month, min(payday_day, last_day))

    if d >= payday_day:
        last_payday = safe_payday(y, m)
        next_payday = safe_payday(y + 1 if m == 12 else y, 1 if m == 12 else m + 1)
    else:
        last_payday = safe_payday(y - 1 if m == 1 else y, 12 if m == 1 else m - 1)
        next_payday = safe_payday(y, m)

    return last_payday, next_payday


def calculate_risk_score(
    predicted_until_payday: float,
    usable_budget: float,
    card_transactions: list[dict],
    category_plans: list[dict],
    payday_day: int = 25,
    today: Optional[datetime] = None,
) -> float:
    """
    risk_score (0.0 ~ 1.0) 계산 — 1차 버전 (3 feature)

    두 축 모두 usable_budget 기준으로 통일:
      predicted_ratio        : 예측 지출 / 플랜 예산
      category_pressure_score: 현재 주기 카테고리 지출 / 배정 예산 (max)
      burndown_deviation_score: 이상적 잔액 대비 실제 잔액 이탈 정도

    점수 구간:
      0.00–0.39 : 안정
      0.40–0.59 : 주의
      0.60–0.79 : 경고
      0.80–1.00 : 고위험
    """
    if today is None:
        today = datetime.now()

    last_payday, next_payday = _get_period_bounds(today, payday_day)

    # ── 현재 급여 주기 거래만 필터링 ──────────────────────────────────────────
    period_txs = []
    for tx in card_transactions:
        if tx.get("type") == "DEPOSIT":
            continue
        amount = int(tx.get("amount", 0))
        if amount <= 0:
            continue
        tx_dt = _parse_tx_date(str(tx.get("date", "")))
        if tx_dt and last_payday <= tx_dt <= today:
            period_txs.append(tx)

    # ── 1. predicted_ratio (0.5) ─────────────────────────────────────────────
    # 월급일까지 예측 지출 / 플랜 예산 — PatchTST 결과 활용
    predicted_ratio = _clamp(predicted_until_payday / (usable_budget + 1))

    # ── 2. category_pressure_score (0.3) ─────────────────────────────────────
    # 현재 주기 내 카테고리별 실제 지출 / 배정 예산 중 최대값
    category_spent: dict[str, int] = defaultdict(int)
    for tx in period_txs:
        category_spent[tx.get("category", "기타")] += int(tx.get("amount", 0))

    max_pressure = 0.0
    for plan in category_plans:
        budget = plan.get("allocatedAmount", 0)
        if budget <= 0:
            continue
        spent = category_spent.get(plan.get("categoryName", ""), 0)
        max_pressure = max(max_pressure, spent / budget)
    category_pressure_score = _clamp(max_pressure)

    # ── 3. burndown_deviation_score (0.2) ────────────────────────────────────
    # (이상적 잔액 − 실제 잔액) / usable_budget — 계획보다 빨리 소비 중인 정도
    burndown_deviation_score = 0.0
    if usable_budget > 0:
        total_days = (next_payday.date() - last_payday.date()).days
        days_elapsed = (today.date() - last_payday.date()).days

        if total_days > 0:
            ideal_remaining = usable_budget * (1.0 - days_elapsed / total_days)
            actual_spent = sum(int(tx.get("amount", 0)) for tx in period_txs)
            actual_remaining = usable_budget - actual_spent

            deviation = (ideal_remaining - actual_remaining) / usable_budget
            # 월초(1~4일)에는 데이터 적어 점수가 과하게 튀므로 완화
            early_month_weight = min(days_elapsed / 5.0, 1.0)
            burndown_deviation_score = _clamp(deviation * 3) * early_month_weight

    # ── 최종 가중합 ───────────────────────────────────────────────────────────
    risk_score = (
        0.5 * predicted_ratio
        + 0.3 * category_pressure_score
        + 0.2 * burndown_deviation_score
    )

    return round(_clamp(risk_score), 3)


def get_budget_strategy(risk_score: float) -> str:
    """risk_score → 예산 전략 문자열 변환."""
    if risk_score >= 0.8:
        return "tight"
    elif risk_score >= 0.5:
        return "balanced"
    return "safe"
