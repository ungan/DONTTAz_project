from __future__ import annotations

from typing import List


class RecommendationService:
    def generate_plan_recommendations(
        self,
        *,
        category_plans: List[dict],
        risk_score: float,
        sealed_amount: int,
        payday_day: int,
    ) -> list[dict]:
        recommendations: list[dict] = []

        top_categories = sorted(
            category_plans,
            key=lambda x: int(x.get("allocatedAmount", 0)),
            reverse=True,
        )

        if top_categories:
            top_category = top_categories[0].get("categoryName")
            top_amount = int(top_categories[0].get("allocatedAmount", 0))
            recommendations.append(
                {
                    "type": "action",
                    "categoryName": top_category,
                    "message": f"{top_category} 예산이 {top_amount:,}원으로 가장 큽니다. 주간 한도를 따로 나눠 관리해보세요.",
                }
            )

            if top_category == "식비":
                recommendations.append(
                    {
                        "type": "habit_change",
                        "categoryName": "식비",
                        "message": "외식·배달 횟수를 주 2~3회 이내로 제한하면 식비 초과를 줄이는 데 도움이 됩니다.",
                    }
                )
            elif top_category == "쇼핑":
                recommendations.append(
                    {
                        "type": "habit_change",
                        "categoryName": "쇼핑",
                        "message": "쇼핑은 결제 전 하루 보류 규칙을 적용해 계획 외 구매를 줄여보세요.",
                    }
                )

        if sealed_amount > 0:
            recommendations.append(
                {
                    "type": "saving_tip",
                    "message": f"급여일까지 {sealed_amount:,}원을 봉인 금액으로 유지하는 것을 추천합니다.",
                }
            )

        if risk_score >= 0.8:
            recommendations.append(
                {
                    "type": "warning",
                    "message": f"현재 위험 점수가 높습니다. 급여일({payday_day}일) 전까지 선택 소비를 최소화하는 것이 좋습니다.",
                }
            )
        elif risk_score >= 0.5:
            recommendations.append(
                {
                    "type": "action",
                    "message": f"급여일({payday_day}일) 전까지는 주간 점검 방식으로 예산 사용 흐름을 확인해보세요.",
                }
            )
        else:
            recommendations.append(
                {
                    "type": "positive",
                    "message": "현재 예산 구조는 비교적 안정적입니다. 카테고리별 한도만 유지해도 무리가 적어 보입니다.",
                }
            )

        return recommendations[:5]
