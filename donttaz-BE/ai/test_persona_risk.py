"""
페르소나 라벨 + risk_score 경향성 검증 테스트

테스트 전략:
  1. 페르소나 라벨 테스트  — 카테고리 분포 → 기대 라벨 정답 비교
  2. risk_score 경향성 테스트 — 절대값이 아닌 상대 비교 (누가 더 높아야 하는지)
  3. strategy 분기 테스트   — risk_score → safe/balanced/tight 분기 확인

주의:
  - normalization_service 기준 실제 카테고리 체계 반영
  - "외식", "배달"은 CATEGORY_KR_MAP에 없으므로 식도락형은 "식비"로만 매칭
  - risk_score 절대값 단언(assert == 0.532) 지양 — 가중치 변경에 취약함
"""

import pytest
from datetime import datetime
from app.domains.report.analyzer import SpendingAnalysisService
from app.domains.risk.scorer import calculate_risk_score, get_budget_strategy


# ─────────────────────────────────────────────────────────────────────────────
# 공통 픽스처 헬퍼
# ─────────────────────────────────────────────────────────────────────────────

TODAY = datetime(2026, 3, 10)   # 월급일(25일) 이전 — 주기 중간
PAYDAY = 25


def make_tx(category: str, amount: int, date: str = "2026-03-05") -> dict:
    return {
        "category": category,
        "amount": amount,
        "type": "WITHDRAWAL",
        "date": date,
    }


def make_category_plan(name: str, budget: int) -> dict:
    return {"categoryName": name, "allocatedAmount": budget}


# ─────────────────────────────────────────────────────────────────────────────
# 1. 페르소나 라벨 테스트
# ─────────────────────────────────────────────────────────────────────────────

class TestPersonaLabel:
    """카테고리 비중 → 페르소나 라벨 정답 비교."""

    svc = SpendingAnalysisService()

    def _txs(self, items: list[tuple[str, int]]) -> list[dict]:
        """(카테고리, 금액) 리스트를 거래 목록으로 변환."""
        return [make_tx(cat, amt) for cat, amt in items]

    def test_식도락형(self):
        # "외식"/"배달"은 실제 카테고리에 없으므로 "식비"로만 매칭됨
        txs = self._txs([("식비", 200_000), ("교통", 30_000), ("기타", 20_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "식도락형"

    def test_쇼핑족(self):
        txs = self._txs([("쇼핑", 300_000), ("식비", 50_000), ("기타", 20_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "쇼핑족"

    def test_생활밀착형(self):
        txs = self._txs([("교통", 150_000), ("생활", 100_000), ("식비", 30_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "생활밀착형"

    def test_여가충전형(self):
        txs = self._txs([("여가", 250_000), ("식비", 40_000), ("기타", 20_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "여가충전형"

    def test_자기계발형(self):
        txs = self._txs([("의료/건강", 200_000), ("교육", 100_000), ("식비", 30_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "자기계발형"

    def test_재무관리형(self):
        txs = self._txs([("금융/보험", 300_000), ("식비", 50_000), ("기타", 20_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "재무관리형"

    def test_균형소비형_쏠림없음(self):
        # 최상위 카테고리 비중 < 30% → 균형소비형
        txs = self._txs([
            ("식비", 100_000), ("쇼핑", 90_000), ("교통", 85_000),
            ("여가", 80_000), ("기타", 70_000),
        ])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "균형소비형"

    def test_데이터부족(self):
        # 거래 3건 미만 → 데이터부족
        txs = self._txs([("식비", 50_000), ("쇼핑", 30_000)])
        result = self.svc.extract_spending_persona(txs)
        assert result["persona"] == "데이터부족"


# ─────────────────────────────────────────────────────────────────────────────
# 2. risk_score 경향성 테스트 (상대 비교)
# ─────────────────────────────────────────────────────────────────────────────

class TestRiskScoreTendency:
    """risk_score 경향성 — 절대값 단언 없이 순서/범위만 검증."""

    def _score(
        self,
        txs: list[dict],
        plans: list[dict],
        predicted: float = 300_000,
        budget: float = 1_000_000,
    ) -> float:
        return calculate_risk_score(
            predicted_until_payday=predicted,
            usable_budget=budget,
            card_transactions=txs,
            category_plans=plans,
            payday_day=PAYDAY,
            today=TODAY,
        )

    # ── 범위 검증 ────────────────────────────────────────────────────────────

    def test_risk_score_범위_0에서1(self):
        txs = [make_tx("쇼핑", 500_000)]
        plans = [make_category_plan("쇼핑", 300_000)]
        score = self._score(txs, plans)
        assert 0.0 <= score <= 1.0

    def test_예측지출이_예산을_크게_초과하면_고위험(self):
        txs = [make_tx("쇼핑", 100_000)]
        plans = [make_category_plan("쇼핑", 300_000)]
        score = self._score(txs, plans, predicted=2_000_000, budget=100_000)
        assert score >= 0.8

    def test_지출없으면_낮은_risk(self):
        score = self._score(txs=[], plans=[], predicted=50_000, budget=1_000_000)
        assert score < 0.4

    # ── 상대 비교 ────────────────────────────────────────────────────────────

    def test_쇼핑족_risk_초과가_재무관리형보다_높음(self):
        """쇼핑 예산 초과 → 안정추구형(금융/보험 정상 지출)보다 위험해야 함."""
        shopping_txs = [make_tx("쇼핑", 450_000)]
        shopping_plans = [make_category_plan("쇼핑", 300_000)]
        shopping_risk = self._score(shopping_txs, shopping_plans)

        stable_txs = [make_tx("금융/보험", 150_000)]
        stable_plans = [make_category_plan("금융/보험", 300_000)]
        stable_risk = self._score(stable_txs, stable_plans)

        assert shopping_risk > stable_risk

    def test_식도락형_예산초과가_생활밀착형_정상보다_높음(self):
        food_txs = [make_tx("식비", 380_000)]
        food_plans = [make_category_plan("식비", 300_000)]
        food_risk = self._score(food_txs, food_plans)

        living_txs = [make_tx("교통", 100_000), make_tx("생활", 80_000)]
        living_plans = [
            make_category_plan("교통", 200_000),
            make_category_plan("생활", 200_000),
        ]
        living_risk = self._score(living_txs, living_plans)

        assert food_risk > living_risk

    def test_균형소비형이_쇼핑족_초과보다_낮음(self):
        balanced_txs = [
            make_tx("식비", 80_000), make_tx("쇼핑", 70_000),
            make_tx("교통", 60_000), make_tx("여가", 50_000),
        ]
        balanced_plans = [
            make_category_plan("식비", 200_000),
            make_category_plan("쇼핑", 200_000),
            make_category_plan("교통", 200_000),
            make_category_plan("여가", 200_000),
        ]
        balanced_risk = self._score(balanced_txs, balanced_plans)

        shopping_txs = [make_tx("쇼핑", 550_000)]
        shopping_plans = [make_category_plan("쇼핑", 300_000)]
        shopping_risk = self._score(shopping_txs, shopping_plans)

        assert balanced_risk < shopping_risk

    def test_예측지출_높을수록_risk_높음(self):
        txs = [make_tx("식비", 100_000)]
        plans = [make_category_plan("식비", 300_000)]

        low_pred_risk = self._score(txs, plans, predicted=100_000, budget=1_000_000)
        high_pred_risk = self._score(txs, plans, predicted=800_000, budget=1_000_000)

        assert high_pred_risk > low_pred_risk


# ─────────────────────────────────────────────────────────────────────────────
# 3. strategy 분기 테스트
# ─────────────────────────────────────────────────────────────────────────────

class TestStrategyBranch:
    """risk_score → safe / balanced / tight 분기 확인 (실제 get_budget_strategy 사용)."""

    def test_safe_구간(self):
        assert get_budget_strategy(0.0) == "safe"
        assert get_budget_strategy(0.39) == "safe"

    def test_balanced_구간(self):
        assert get_budget_strategy(0.5) == "balanced"
        assert get_budget_strategy(0.79) == "balanced"

    def test_tight_구간(self):
        assert get_budget_strategy(0.8) == "tight"
        assert get_budget_strategy(1.0) == "tight"

    def test_경계값_0_5(self):
        assert get_budget_strategy(0.5) == "balanced"

    def test_경계값_0_8(self):
        assert get_budget_strategy(0.8) == "tight"
