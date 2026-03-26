from __future__ import annotations

import os
import json
from typing import List

from openai import OpenAI


class LLMRecommendationService:
    def __init__(self):
        self.client = OpenAI(
            api_key=os.environ.get("GMS_KEY"),
            base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1",
        )

    def _build_key_signals(
        self,
        spending_analysis: dict,
        weekday_pattern: dict | None,
        persona_shift: dict | None,
    ) -> list[str]:
        signals = []

        # 1. 과도 + 급증 카테고리 (가장 중요)
        overdone = []
        surging = []
        decreasing = []
        for cat, data in spending_analysis.items():
            rate = data.get("증감률")
            is_overdone = data.get("과도여부", False)
            curr = data.get("이번달", 0)
            prev = data.get("전달", 0)

            if is_overdone and rate and rate > 20:
                overdone.append((cat, rate, curr, prev))
            elif is_overdone:
                overdone.append((cat, None, curr, prev))
            elif rate and rate > 30:
                surging.append((cat, rate, curr, prev))
            elif rate and rate < -20 and prev > 0:
                decreasing.append((cat, rate, curr, prev))

        overdone.sort(key=lambda x: x[1] or 0, reverse=True)
        for cat, rate, curr, prev in overdone[:2]:
            rate_str = f" ({rate:+.0f}%)" if rate else ""
            signals.append(f"[경고] {cat} 권장 비중 초과{rate_str} — 이번달 {curr:,}원 / 전달 {prev:,}원")

        for cat, rate, curr, prev in surging[:1]:
            signals.append(f"[급증] {cat} 전달 대비 +{rate:.0f}% — 이번달 {curr:,}원 / 전달 {prev:,}원")

        for cat, rate, curr, prev in decreasing[:1]:
            signals.append(f"[긍정] {cat} 전달 대비 {rate:.0f}% 감소 — 잘 관리하고 있음")

        # 2. 페르소나 변화
        if persona_shift and persona_shift.get("shifted"):
            prev_p = persona_shift["previousPersona"]
            curr_p = persona_shift["currentPersona"]
            signals.append(f"[변화] 소비 성향 {prev_p} → {curr_p} (이번달 소비 패턴 전환)")

        # 3. 요일 패턴
        if weekday_pattern and weekday_pattern.get("topIncrease"):
            top = weekday_pattern["topIncrease"]
            if abs(top["change"]) >= 5:
                signals.append(f"[패턴] {top['weekday']}요일 지출 비중 +{top['change']}%p 집중 (전달 {top['prevRatio']}% → 이번달 {top['currentRatio']}%)")

        return signals

    def generate_recommendations(
        self,
        spending_analysis: dict,
        risk_score: float,
        sealed_amount: int,
        payday_day: int,
        monthly_income: int,
        spending_persona: dict | None = None,
        weekday_pattern: dict | None = None,
        persona_shift: dict | None = None,
    ) -> dict:
        try:
            persona_str = ""
            if spending_persona and spending_persona.get("persona") not in ("분석불가", "데이터부족"):
                persona = spending_persona.get("persona", "균형소비형")
                topics = spending_persona.get("topics", [])
                top_keywords = []
                for t in sorted(topics, key=lambda x: x.get("weight", 0), reverse=True)[:2]:
                    top_keywords.extend(t.get("keywords", []))
                persona_str = f"\n소비_페르소나: {persona} (주요 소비 패턴: {', '.join(top_keywords[:4])})"

            if persona_shift and persona_shift.get("shifted"):
                persona_str += f"\n페르소나_변화: {persona_shift['previousPersona']} → {persona_shift['currentPersona']} (이번달 소비 성향이 바뀜)"

            weekday_str = ""
            if weekday_pattern and weekday_pattern.get("topIncrease"):
                top_inc = weekday_pattern["topIncrease"]
                top_dec = weekday_pattern["topDecrease"]
                weekday_str = f"\n요일_패턴: {top_inc['weekday']}요일 지출 비중 +{top_inc['change']}%p 증가, {top_dec['weekday']}요일 {top_dec['change']}%p 감소"

            key_signals = self._build_key_signals(spending_analysis, weekday_pattern, persona_shift)

            prompt_data = {
                "핵심_소비_신호": key_signals,
                "카테고리별_상세": spending_analysis,
                "위험점수": risk_score,
                "봉인금액": sealed_amount,
                "월급일": f"{payday_day}일",
                "월소득": monthly_income,
            }

            system_prompt = f"""당신은 개인 재무 분석 AI입니다. 사용자의 소비 데이터를 분석하여 개인화된 예산 조언을 한국어로 제공합니다.{persona_str}{weekday_str}

반드시 다음 JSON 형식으로만 응답하세요:
{{
  "categoryComments": {{
    "카테고리명": "해당 카테고리에 대한 구체적이고 개인화된 한 문장 조언 (실제 금액과 증감률 언급)"
  }},
  "recommendations": [
    {{"type": "warning|action|habit_change|saving_tip|positive", "categoryName": "카테고리명 또는 null", "message": "구체적인 조언 메시지"}}
  ]
}}

조언 원칙:
- 핵심_소비_신호를 최우선으로 참고하여 가장 중요한 신호부터 언급
- 실제 숫자(금액, 증감률, 비중)를 직접 인용하여 구체적으로 작성
- [경고] 신호는 절감 방법을 구체적으로 제시 (배달 줄이기, 구매 지연 규칙 등)
- [긍정] 신호는 칭찬하며 유지 동기 부여
- [변화] 신호가 있으면 habit_change 타입으로 소비 성향 변화 언급
- [패턴] 신호가 있으면 해당 요일 집중 원인과 관리 팁 제시
- 위험점수 0.8 이상이면 warning 타입 추천 반드시 포함
- 소비 페르소나 특성에 맞는 절감 전략 1개 포함
- recommendations는 3~4개, categoryComments는 지출이 있는 카테고리만 포함"""

        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "developer", "content": system_prompt},
                    {"role": "user", "content": json.dumps(prompt_data, ensure_ascii=False)},
                ],
                response_format={"type": "json_object"},
                temperature=0.7,
            )
            return json.loads(response.choices[0].message.content)
        except Exception:
            return {}


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
