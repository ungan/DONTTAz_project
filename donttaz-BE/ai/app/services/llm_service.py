import json
import os

try:
    from openai import OpenAI
except ImportError:
    OpenAI = None


class LLMService:
    def __init__(self):
        self.enabled = os.getenv("LLM_ENABLED", "false").lower() == "true"
        self.api_key = os.getenv("GMS_KEY")

        if self.enabled and self.api_key and OpenAI is not None:
            self.client = OpenAI(
                api_key=self.api_key,
                base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1",
            )
        else:
            self.client = None

    def generate_overall_feedback(self, signals: dict) -> str | None:
        if signals.get("isDataInsufficient"):
            return None

        if not self.enabled or not self.client:
            return None

        prompt = self._build_prompt(signals)

        try:
            response = self.client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "user", "content": prompt}
                ],
                temperature=0.3,
                max_tokens=120,
            )

            content = response.choices[0].message.content
            if not content:
                return None

            return content.strip()

        except Exception as e:
            print(f"LLM 호출 중 오류 발생: {e}")
            return None

    def _build_prompt(self, signals: dict) -> str:
        compact_signals = {
            "isDataInsufficient": signals.get("isDataInsufficient"),
            "totalBudget": signals.get("totalBudget"),
            "totalSpending": signals.get("totalSpending"),
            "remainingBudget": signals.get("remainingBudget"),
            "achievementRate": signals.get("achievementRate"),
            "patternChange": signals.get("patternChange"),
            "topCategory": signals.get("topCategory"),
            "overspentCategories": signals.get("overspentCategories"),
            "nightSpendingRatio": signals.get("nightSpendingRatio"),
            "impulseScore": signals.get("impulseScore"),
            "riskLevel": signals.get("riskLevel"),
            "spendingPersona": signals.get("spendingPersona"),
        }

        return f"""
당신은 금융 소비 리포트용 한국어 코멘트를 작성하는 도우미입니다.

규칙:
- 과장하지 말 것
- 공포를 유발하지 말 것
- 숫자와 상태를 기반으로 2문장 이내로 작성할 것
- 사용자가 바로 이해할 수 있는 자연스러운 한국어로 작성할 것
- 조언은 1개만 포함할 것
- 데이터가 부족하면 '정확한 해석이 어렵다'는 취지로 매우 보수적으로 작성할 것
- category 이름은 자연스럽게 한국어 문장 안에 녹여 쓸 것
- spendingPersona가 있으면 반드시 "당신은 이번 달 ○○ 소비 패턴을 보였습니다."로 시작할 것
- 출력은 순수 문장만, JSON/따옴표/불릿 금지

입력 데이터:
{json.dumps(compact_signals, ensure_ascii=False)}
""".strip()