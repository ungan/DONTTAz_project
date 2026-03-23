from __future__ import annotations


class PersonalizedFeedbackService:
    def build(self, *, report: dict, category_reports: list[dict], spending_persona: dict | None = None) -> list[dict]:
        summary = report.get("summary", {})
        risk = report.get("risk", {})

        items: list[dict] = []

        if spending_persona and spending_persona.get("persona") not in (None, "분석불가", "데이터부족"):
            persona = spending_persona["persona"]
            items.append({
                "type": "persona",
                "message": f"당신은 이번 달 {persona} 소비 패턴을 보였습니다.",
            })

        overspent = [
            item for item in category_reports
            if int(item.get("allocatedAmount", 0)) > 0
            and int(item.get("spentAmount", 0)) > int(item.get("allocatedAmount", 0))
        ]
        for item in overspent[:2]:
            over_amount = int(item["spentAmount"]) - int(item["allocatedAmount"])
            items.append(
                {
                    "type": "warning",
                    "categoryName": item["categoryName"],
                    "message": f"{item['categoryName']}가 계획 대비 {over_amount:,}원 초과되었습니다.",
                }
            )

        pattern_change = str(summary.get("patternChange", "") or "").strip()
        if pattern_change:
            items.append(
                {
                    "type": "pattern",
                    "message": pattern_change,
                }
            )

        stable_categories = [
            item for item in category_reports
            if int(item.get("allocatedAmount", 0)) > 0
            and int(item.get("spentAmount", 0)) <= int(item.get("allocatedAmount", 0))
        ]
        if stable_categories:
            best = min(
                stable_categories,
                key=lambda x: abs(int(x.get("allocatedAmount", 0)) - int(x.get("spentAmount", 0))),
            )
            items.append(
                {
                    "type": "positive",
                    "categoryName": best["categoryName"],
                    "message": f"{best['categoryName']}는 계획 범위 내에서 안정적으로 관리되었습니다.",
                }
            )

        night_ratio = float(risk.get("nightSpendingRatio", 0) or 0)
        if night_ratio >= 15:
            items.append(
                {
                    "type": "pattern",
                    "message": "야간 소비 비중이 높았습니다. 늦은 시간 결제를 줄이면 변동지출 관리에 도움이 됩니다.",
                }
            )

        deduped: list[dict] = []
        seen: set[str] = set()
        for item in items:
            key = f"{item.get('type')}|{item.get('categoryName')}|{item.get('message')}"
            if key in seen:
                continue
            seen.add(key)
            deduped.append(item)

        return deduped[:5]
