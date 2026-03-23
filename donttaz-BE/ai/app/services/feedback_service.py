from app.services.llm_service import LLMService


class FeedbackService:
    def __init__(self):
        self.llm_service = LLMService()

    def _build_signals(self, report: dict, spending_persona: dict | None = None) -> dict:
        summary = report.get("summary", {})
        categories = report.get("categories", {})
        risk = report.get("risk", {})

        total_budget = summary.get("totalBudget", 0)
        total_spending = summary.get("totalSpending", 0)
        remaining_budget = summary.get("remainingBudget", 0)
        achievement_rate = summary.get("achievementRate", 0)
        pattern_change = summary.get("patternChange", "")

        top_categories = categories.get("topCategories", [])
        top_category = top_categories[0].get("category") if top_categories else None

        category_budget_status = categories.get("categoryBudgetStatus", {})
        overspent_categories = [
            category
            for category, value in category_budget_status.items()
            if value.get("remaining", 0) < 0
        ]

        night_ratio = risk.get("nightSpendingRatio", 0)
        impulse_score = risk.get("impulseScore", 0)
        risk_level = risk.get("riskLevel", "LOW")

        is_data_insufficient = (
            total_budget <= 0 or total_spending <= 0
        )

        persona = None
        if spending_persona and spending_persona.get("persona") not in (None, "분석불가", "데이터부족"):
            persona = spending_persona["persona"]

        return {
            "totalBudget": total_budget,
            "totalSpending": total_spending,
            "remainingBudget": remaining_budget,
            "achievementRate": achievement_rate,
            "patternChange": pattern_change,
            "topCategory": top_category,
            "overspentCategories": overspent_categories,
            "nightSpendingRatio": night_ratio,
            "impulseScore": impulse_score,
            "riskLevel": risk_level,
            "isDataInsufficient": is_data_insufficient,
            "spendingPersona": persona,
        }

    def _build_fallback_feedback(self, signals: dict) -> dict:
        feedback = []

        is_data_insufficient = signals["isDataInsufficient"]
        remaining_budget = signals["remainingBudget"]
        achievement_rate = signals["achievementRate"]
        pattern_change = signals["patternChange"]
        top_category = signals["topCategory"]
        overspent_categories = signals["overspentCategories"]
        night_ratio = signals["nightSpendingRatio"]
        impulse_score = signals["impulseScore"]
        risk_level = signals["riskLevel"]

        if is_data_insufficient:
            overall = "소비 또는 예산 데이터가 아직 충분하지 않아 보수적으로 해석하고 있습니다."
            feedback.append("소비 또는 예산 데이터가 아직 충분하지 않습니다.")
            return {
                "feedback": feedback,
                "overallFeedback": overall,
            }

        # 1. 예산 상태
        if remaining_budget < 0:
            feedback.append("현재 예산을 초과해 소비하고 있어 지출 조정이 필요합니다.")
        elif remaining_budget == 0:
            feedback.append("예산을 거의 모두 사용했습니다. 남은 기간 소비를 보수적으로 관리해보세요.")
        else:
            feedback.append("현재까지는 예산 범위 내에서 소비를 관리하고 있습니다.")

        # 2. 예산 사용률
        if achievement_rate >= 100:
            feedback.append("이번 달 예산을 초과했습니다. 상위 지출 항목을 우선 점검해보세요.")
        elif achievement_rate >= 80:
            feedback.append("예산 사용률이 높은 편입니다. 남은 기간 동안 선택 소비를 줄이는 것이 좋습니다.")
        elif achievement_rate >= 60:
            feedback.append("예산 사용률이 점차 높아지고 있습니다. 남은 기간 지출 흐름을 함께 관리해보세요.")

        # 3. 카테고리 초과
        if overspent_categories:
            feedback.append(
                f"일부 카테고리에서 예산 초과가 발생했습니다: {', '.join(overspent_categories)}."
            )

        # 4. 상위 카테고리
        if top_category:
            feedback.append(f"가장 지출이 큰 카테고리는 '{top_category}'입니다.")

            if top_category == "식비":
                feedback.append("식비 지출이 높습니다. 배달·외식 빈도를 점검하고 주간 식비 한도를 정해보세요.")
            elif top_category == "쇼핑":
                feedback.append("쇼핑 지출 비중이 높습니다. 구매 전 하루 보류 규칙을 적용해보세요.")
            elif top_category == "교통":
                feedback.append("교통 관련 소비가 큰 편입니다. 대중교통·주유·주차비 비중을 함께 점검해보세요.")
            elif top_category == "생활":
                feedback.append("생활 지출이 높습니다. 생필품·마트·편의점 소비가 누적되지 않았는지 확인해보세요.")
            elif top_category == "여가":
                feedback.append("여가 지출 비중이 높습니다. 정기 구독이나 취미성 지출을 함께 점검해보세요.")
            elif top_category == "의료/건강":
                feedback.append("의료·건강 지출은 필요 지출일 수 있으므로 일시적 지출인지 지속 지출인지 구분해보세요.")
            elif top_category == "교육":
                feedback.append("교육 지출은 자기계발 성격이 강해 별도 한도로 관리하면 좋습니다.")
            elif top_category == "금융/보험":
                feedback.append("금융·보험 지출은 고정지출일 수 있으니 납부 주기와 금액을 점검해보세요.")
            elif top_category == "경조사/선물":
                feedback.append("경조사·선물 지출은 월별 변동폭이 클 수 있어 별도 관리가 필요합니다.")
            elif top_category == "기타":
                feedback.append("기타 지출이 많습니다. 세부 내역을 다시 확인해보세요.")

        # 5. 야간/충동/위험도
        if night_ratio >= 30:
            feedback.append("야간 소비 비율이 높습니다. 늦은 시간 결제를 줄이면 충동 소비를 낮출 수 있습니다.")
        elif night_ratio >= 15:
            feedback.append("야간 소비가 다소 있습니다. 소비 시간대를 점검해보는 것을 추천합니다.")

        if impulse_score >= 70:
            feedback.append("충동 소비 지수가 높은 편입니다. 구매 지연 규칙을 활용해보세요.")
        elif impulse_score >= 40:
            feedback.append("충동 소비 경향이 일부 보입니다. 반복 지출 패턴을 체크해보세요.")

        if risk_level == "HIGH":
            feedback.append("현재 소비 위험도가 높아 즉시 지출 관리가 필요합니다.")
        elif risk_level == "MEDIUM":
            feedback.append("소비 패턴에 주의가 필요한 상태입니다.")
        else:
            if "집중" not in pattern_change and "감소하는 흐름" not in pattern_change:
                feedback.append("전반적인 소비 패턴은 비교적 안정적입니다.")

        if pattern_change and "안정적" not in pattern_change:
            feedback.append(pattern_change)

        # overall
        if remaining_budget < 0:
            overall = "이번 달 예산을 초과했습니다. 상위 지출 항목과 소비 시간대를 우선 점검해보세요."
        elif achievement_rate < 30:
            overall = "현재 소비는 안정적인 초기 단계입니다. 예산 범위를 유지하면 무리 없이 관리할 수 있습니다."
        elif overspent_categories:
            overall = "전체 예산 범위는 유지하고 있지만, 일부 카테고리에서 예산 초과 소비가 발생했습니다."
        elif risk_level == "HIGH":
            overall = "현재 소비 위험도가 높은 상태입니다. 변동지출을 줄이고 소비 패턴을 즉시 점검하는 것이 좋습니다."
        elif risk_level == "MEDIUM":
            if night_ratio >= 15:
                overall = "전체 예산 범위는 유지하고 있지만, 야간 소비와 일부 지출 패턴에 대한 점검이 필요합니다."
            else:
                overall = "전체 예산 범위는 유지하고 있지만, 일부 소비 패턴에 대한 점검이 필요합니다."
        elif achievement_rate >= 80:
            overall = "예산 사용률이 높은 편입니다. 남은 기간 동안 선택 소비를 줄이는 것이 좋습니다."
        elif pattern_change and "집중" in pattern_change:
            overall = "전체적으로는 안정적이지만, 특정 주차에 소비가 집중되는 패턴이 나타났습니다."
        else:
            overall = "현재까지는 비교적 안정적인 소비 흐름을 보이고 있습니다."

        persona = signals.get("spendingPersona")
        if persona:
            overall = f"당신은 이번 달 {persona} 소비 패턴을 보였습니다. {overall}"

        return {
            "feedback": feedback,
            "overallFeedback": overall,
        }

    def generate_feedback(self, report: dict, spending_persona: dict | None = None) -> dict:
        signals = self._build_signals(report, spending_persona)

        fallback_result = self._build_fallback_feedback(signals)

        llm_result = self.llm_service.generate_overall_feedback(signals)
        if llm_result:
            fallback_result["overallFeedback"] = llm_result

        return fallback_result