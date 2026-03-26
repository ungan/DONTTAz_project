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

[속성 정의 - Schema]
- totalBudget: 이번 달 총 예산 (원)
- totalSpending: 이번 달 총 지출 (원)
- remainingBudget: 잔여 예산 (음수이면 초과 상태)
- achievementRate: 예산 사용률 (0~100+, 100 이상이면 초과)
- patternChange: 소비 패턴 변화 요약 문자열
- topCategory: 지출이 가장 높은 카테고리
- overspentCategories: 예산 초과된 카테고리 목록
- nightSpendingRatio: 야간(22~6시) 소비 비율 (0~100, 30 이상이면 높음)
- impulseScore: 충동 소비 지수 (0~100, 70 이상이면 높음, 40 이상이면 주의)
- riskLevel: 소비 위험도 (LOW / MEDIUM / HIGH)
- spendingPersona: 이번 달 소비 유형 요약 (없으면 null)

[제약 조건 - Constraints]
# 단일 속성 (Unary)
- achievementRate >= 100 이면 → 예산 초과 상태로 해석할 것
- impulseScore >= 70 이면 → 충동 소비 높음으로 해석할 것
- nightSpendingRatio >= 30 이면 → 야간 소비 높음으로 해석할 것
- riskLevel == HIGH 이면 → 즉각적인 지출 관리 필요로 해석할 것

# 속성 간 관계 (Binary)
- remainingBudget < 0 이면 achievementRate >= 100 임 (두 신호가 불일치하면 remainingBudget 우선)
- overspentCategories가 비어있지 않으면 → 반드시 해당 카테고리를 코멘트에 포함할 것
- riskLevel == HIGH 이면 → impulseScore 또는 nightSpendingRatio 중 하나가 높은 상태임

[출력 규칙]
- 과장하지 말 것, 공포를 유발하지 말 것
- 2문장 이내, 조언은 1개만 포함
- 자연스러운 한국어로 작성
- spendingPersona가 있으면 반드시 "당신은 이번 달 ○○ 소비 패턴을 보였습니다."로 시작할 것
- 출력은 순수 문장만 (JSON/따옴표/불릿 금지)

[예시 - In-context Examples]
입력: achievementRate=45, riskLevel=LOW, topCategory=식비, overspentCategories=[]
출력: 현재 식비 위주로 예산의 절반 정도를 사용했습니다. 이 흐름이라면 월말까지 무리 없이 관리할 수 있습니다.

입력: achievementRate=105, riskLevel=HIGH, overspentCategories=[쇼핑, 여가], impulseScore=75
출력: 이번 달 예산을 초과했으며, 특히 쇼핑과 여가 지출이 계획을 넘었습니다. 남은 기간 선택 지출을 최소화하는 것을 권장합니다.

입력: achievementRate=72, riskLevel=MEDIUM, nightSpendingRatio=35, spendingPersona=야간 집중형
출력: 당신은 이번 달 야간 집중형 소비 패턴을 보였습니다. 야간 결제 비중이 높은 편이므로 늦은 시간 충동 구매를 줄이면 지출을 효과적으로 낮출 수 있습니다.

[입력 데이터]
{json.dumps(compact_signals, ensure_ascii=False)}
""".strip()
