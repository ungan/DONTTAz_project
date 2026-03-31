from collections import defaultdict
from datetime import datetime


class SpendingAnalysisService:
    def _get_week_label(self, day: int) -> str:
        if day <= 7:
            return "1주차"
        elif day <= 14:
            return "2주차"
        elif day <= 21:
            return "3주차"
        elif day <= 28:
            return "4주차"
        return "5주차"

    def _aggregate(self, transactions):
        expense_txs = [tx for tx in transactions if tx["type"] == "WITHDRAWAL" and int(tx.get("amount", 0)) <= 500000]

        total_spending = 0
        weekly_spending = defaultdict(int)
        active_days = set()
        weekday_spending = defaultdict(int)
        hour_spending = defaultdict(int)
        category_spending = defaultdict(int)
        merchant_count = defaultdict(int)
        merchant_amount = defaultdict(int)
        weekly_category_spending = defaultdict(lambda: defaultdict(int))

        night_spending = 0
        weekend_spending = 0
        discretionary_spending = 0

        discretionary_categories = {
            "쇼핑", "카페", "외식", "배달", "편의점", "여가",
            "entertainment", "shopping", "leisure"
        }

        parsed = []
        for tx in expense_txs:
            dt = datetime.fromisoformat(tx["date"])
            amount = abs(int(tx["amount"]))
            category = tx["category"]
            merchant = tx["merchant"]

            parsed.append((dt, amount, category, merchant))

            total_spending += amount
            active_days.add(dt.date())

            week = self._get_week_label(dt.day)
            weekly_spending[week] += amount
            weekly_category_spending[week][category] += amount

            weekday = dt.strftime("%A")
            weekday_spending[weekday] += amount

            hour_spending[dt.hour] += amount
            category_spending[category] += amount
            merchant_count[merchant] += 1
            merchant_amount[merchant] += amount

            if dt.hour >= 22 or dt.hour < 5:
                night_spending += amount

            if dt.weekday() >= 5:
                weekend_spending += amount

            if category in discretionary_categories:
                discretionary_spending += amount

        parsed.sort(key=lambda x: x[0])

        daily_avg = round(total_spending / len(active_days)) if active_days else 0

        category_ratio = {}
        if total_spending > 0:
            for cat, amt in category_spending.items():
                category_ratio[cat] = round((amt / total_spending) * 100, 1)

        top_categories = sorted(
            category_spending.items(),
            key=lambda x: x[1],
            reverse=True
        )[:3]

        top_merchants = sorted(
            merchant_count.items(),
            key=lambda x: (x[1], merchant_amount[x[0]]),
            reverse=True
        )[:5]

        return {
            "total_spending": total_spending,
            "daily_average_spending": daily_avg,
            "weekly_spending": dict(weekly_spending),
            "weekday_spending": dict(weekday_spending),
            "hour_spending": dict(hour_spending),
            "category_spending": dict(category_spending),
            "category_ratio": category_ratio,
            "top_categories": [
                {"category": cat, "amount": amt}
                for cat, amt in top_categories
            ],
            "top_merchants": [
                {
                    "merchant": merchant,
                    "count": merchant_count[merchant],
                    "amount": merchant_amount[merchant]
                }
                for merchant, _ in top_merchants
            ],
            "weekly_category_spending": {
                week: dict(cat_map)
                for week, cat_map in weekly_category_spending.items()
            },
            "night_spending": night_spending,
            "weekend_spending": weekend_spending,
            "discretionary_spending": discretionary_spending,
            "parsed": parsed,
        }

    def _build_pattern_change(self, weekly_spending: dict) -> str:
        order = ["1주차", "2주차", "3주차", "4주차", "5주차"]
        week_items = [(w, weekly_spending.get(w, 0)) for w in order if w in weekly_spending]

        if len(week_items) < 2:
            return "주차별 변화 분석을 위한 데이터가 충분하지 않습니다."

        values = [amount for _, amount in week_items]
        total = sum(values)

        if total == 0:
            return "주차별 소비 데이터가 없습니다."

        max_week, max_value = max(week_items, key=lambda x: x[1])

        # 특정 주차 집중 소비
        if max_value / total >= 0.4:
            max_index = [w for w, _ in week_items].index(max_week)

            # 집중된 주 이후 감소 흐름 확인
            if max_index < len(values) - 1:
                after_values = values[max_index + 1:]
                if after_values and sum(after_values) < max_value:
                    return f"{max_week}에 지출이 집중된 이후 후반부에는 소비가 감소하는 흐름을 보였습니다."

            return f"{max_week}에 지출이 집중되는 패턴이 보입니다."

        # 최근 주차 급증
        last_week, last_value = week_items[-1]
        prev_week, prev_value = week_items[-2]

        if prev_value > 0:
            change_rate = (last_value - prev_value) / prev_value
            if change_rate >= 0.3:
                return "최근 주차 지출이 직전 주차보다 크게 증가했습니다."
            elif change_rate <= -0.3:
                return "최근 주차 지출이 직전 주차보다 크게 감소했습니다."

        # 전체적으로 완만한 감소
        if values == sorted(values, reverse=True):
            return "주차가 지날수록 지출이 점차 감소하는 흐름을 보였습니다."

        # 전체적으로 완만한 증가
        if values == sorted(values):
            return "주차가 지날수록 지출이 점차 증가하는 흐름을 보였습니다."

        return "주차별 지출 흐름은 비교적 안정적으로 유지되었습니다."

    def _category_delta(self, current_category_spending, previous_category_spending):
        all_categories = set(current_category_spending.keys()) | set(previous_category_spending.keys())
        result = {}

        for category in all_categories:
            current_amt = current_category_spending.get(category, 0)
            previous_amt = previous_category_spending.get(category, 0)

            if previous_amt == 0 and current_amt > 0:
                change_rate = None
                change_type = "신규 지출"
            elif previous_amt == 0 and current_amt == 0:
                change_rate = 0.0
                change_type = "변화 없음"
            else:
                change_rate = round(((current_amt - previous_amt) / previous_amt) * 100, 1)
                if change_rate > 0:
                    change_type = "증가"
                elif change_rate < 0:
                    change_type = "감소"
                else:
                    change_type = "변화 없음"

            result[category] = {
                "current": current_amt,
                "previous": previous_amt,
                "changeRate": change_rate,
                "changeType": change_type,
            }

        return result

    def _compute_impulse_score(self, total_spending, night_spending, discretionary_spending, weekend_spending):
        if total_spending == 0:
            return 0

        night_ratio = night_spending / total_spending
        discretionary_ratio = discretionary_spending / total_spending
        weekend_ratio = weekend_spending / total_spending

        score = (
            0.4 * night_ratio +
            0.4 * discretionary_ratio +
            0.2 * weekend_ratio
        ) * 100

        return round(min(score, 100), 1)

    def _find_top_change_categories(self, delta_map: dict):
        increase_candidates = []
        decrease_candidates = []

        for category, value in delta_map.items():
            rate = value.get("changeRate")
            if rate is None:
                continue
            if rate > 0:
                increase_candidates.append((category, rate))
            elif rate < 0:
                decrease_candidates.append((category, rate))

        top_increase = None
        top_decrease = None

        if increase_candidates:
            cat, rate = max(increase_candidates, key=lambda x: x[1])
            top_increase = {"category": cat, "changeRate": rate}

        if decrease_candidates:
            cat, rate = min(decrease_candidates, key=lambda x: x[1])
            top_decrease = {"category": cat, "changeRate": rate}

        return top_increase, top_decrease

    # 통계청 2024 가계동향조사 기반 비율 (budget_estimation_service와 동일하게 유지)
    DEFAULT_CATEGORY_RATIO = {
        "식비": 0.28, "쇼핑": 0.12, "교통": 0.11, "생활": 0.12,
        "여가": 0.08, "의료/건강": 0.08, "교육": 0.05,
        "금융/보험": 0.10, "경조사/선물": 0.04, "기타": 0.02,
    }

    PERSONA_MAP = [
        ({"식비", "외식", "배달"}, "식도락형"),
        ({"쇼핑"}, "쇼핑족"),
        ({"교통", "생활"}, "생활밀착형"),
        ({"여가"}, "여가충전형"),
        ({"의료/건강", "교육"}, "자기계발형"),
        ({"금융/보험"}, "재무관리형"),
    ]

    def _map_to_persona(self, keywords: list[str]) -> str:
        keyword_set = set(keywords)
        for category_set, persona in self.PERSONA_MAP:
            if keyword_set & category_set:
                return persona
        return "균형소비형"

    def extract_spending_persona(self, transactions: list[dict]) -> dict:
        """카테고리 지출 비중 기반 소비 페르소나 도출"""
        expense_txs = [
            tx for tx in transactions
            if tx.get("type") == "WITHDRAWAL" and 0 < int(tx.get("amount", 0)) <= 500000
        ]
        if len(expense_txs) < 3:
            return {"persona": "데이터부족", "topics": []}

        category_spending: dict = defaultdict(int)
        total = 0
        for tx in expense_txs:
            cat = tx.get("category") or "기타"
            amount = int(tx.get("amount", 0))
            category_spending[cat] += amount
            total += amount

        if total == 0:
            return {"persona": "분석불가", "topics": []}

        sorted_cats = sorted(category_spending.items(), key=lambda x: x[1], reverse=True)
        ratios = [(cat, amount / total) for cat, amount in sorted_cats]

        # 비중 15% 이상인 카테고리를 상위 카테고리로 판단
        top_categories = [cat for cat, ratio in ratios if ratio >= 0.15]
        if not top_categories:
            top_categories = [ratios[0][0]]

        # 최상위 카테고리 비중이 30% 미만이면 균형소비형
        if ratios[0][1] < 0.30:
            persona = "균형소비형"
        else:
            persona = self._map_to_persona(top_categories)

        return {
            "persona": persona,
            "topics": [
                {"keywords": [cat], "weight": round(ratio, 3)}
                for cat, ratio in ratios[:3]
            ],
        }

    def analyze_weekday_pattern(self, transactions: list[dict]) -> dict:
        """월별 요일 지출 비중 비교 - 이번달 어느 요일에 지출이 집중됐는지 전달 대비 분석."""
        monthly_weekday: dict = defaultdict(lambda: defaultdict(int))
        monthly_total: dict = defaultdict(int)

        for tx in transactions:
            if tx.get("type") == "WITHDRAWAL":
                amount = int(tx.get("amount", 0))
                if amount <= 0:
                    continue
                date_str = str(tx.get("date", ""))[:10]
                try:
                    dt = datetime.fromisoformat(date_str)
                    ym = date_str[:7]
                    monthly_weekday[ym][dt.weekday()] += amount
                    monthly_total[ym] += amount
                except Exception:
                    continue

        if len(monthly_weekday) < 2:
            return {}

        months = sorted(monthly_weekday.keys())
        curr_ym, prev_ym = months[-1], months[-2]
        curr_total = monthly_total[curr_ym] or 1
        prev_total = monthly_total[prev_ym] or 1

        weekday_names = ["월", "화", "수", "목", "금", "토", "일"]
        shifts = {}
        for wd in range(7):
            curr_ratio = monthly_weekday[curr_ym][wd] / curr_total
            prev_ratio = monthly_weekday[prev_ym][wd] / prev_total
            shifts[weekday_names[wd]] = {
                "currentRatio": round(curr_ratio * 100, 1),
                "prevRatio": round(prev_ratio * 100, 1),
                "change": round((curr_ratio - prev_ratio) * 100, 1),
            }

        top_increase = max(shifts.items(), key=lambda x: x[1]["change"])
        top_decrease = min(shifts.items(), key=lambda x: x[1]["change"])

        return {
            "weekdayShifts": shifts,
            "topIncrease": {"weekday": top_increase[0], **top_increase[1]},
            "topDecrease": {"weekday": top_decrease[0], **top_decrease[1]},
        }

    def analyze_persona_shift(self, transactions: list[dict]) -> dict:
        """월별 소비 페르소나 변화 감지 - 이번달 소비 성향이 전달과 달라졌는지 분석."""
        monthly_txs: dict = defaultdict(list)
        for tx in transactions:
            ym = str(tx.get("date", ""))[:7]
            if ym:
                monthly_txs[ym].append(tx)

        if len(monthly_txs) < 2:
            return {}

        months = sorted(monthly_txs.keys())
        persona_history = []
        for ym in months:
            result = self.extract_spending_persona(monthly_txs[ym])
            persona = result.get("persona", "분석불가")
            persona_history.append({"month": ym, "persona": persona})

        curr_persona = persona_history[-1]["persona"]
        prev_persona = persona_history[-2]["persona"]
        invalid = {"분석불가", "데이터부족"}
        shifted = curr_persona not in invalid and prev_persona not in invalid and curr_persona != prev_persona

        return {
            "personaHistory": persona_history,
            "currentPersona": curr_persona,
            "previousPersona": prev_persona,
            "shifted": shifted,
        }

    def analyze_spending_trends(self, card_transactions: list[dict]) -> dict:
        """카테고리별 이번달 vs 전달 증감, 권장 비중 대비 과도 여부 분석."""
        monthly: dict = {}
        for tx in card_transactions:
            if tx.get("type") == "DEPOSIT":
                continue
            amount = int(tx.get("amount", 0))
            if amount <= 0:
                continue
            ym = str(tx.get("date", ""))[:7]
            if not ym or len(ym) < 7:
                continue
            category = tx.get("category") or "기타"
            if category == "수입":
                continue
            monthly.setdefault(ym, {})
            monthly[ym][category] = monthly[ym].get(category, 0) + amount

        if len(monthly) < 2:
            return {}

        months = sorted(monthly.keys())
        curr = monthly[months[-1]]
        prev = monthly[months[-2]]
        curr_total = sum(curr.values()) or 1

        analysis = {}
        for category in set(curr) | set(prev):
            curr_amt = curr.get(category, 0)
            prev_amt = prev.get(category, 0)
            change_ratio = round((curr_amt - prev_amt) / prev_amt * 100, 1) if prev_amt > 0 else None
            curr_ratio = round(curr_amt / curr_total * 100, 1)
            default_ratio = round(self.DEFAULT_CATEGORY_RATIO.get(category, 0.02) * 100, 1)
            analysis[category] = {
                "이번달": curr_amt,
                "전달": prev_amt,
                "증감률": change_ratio,
                "전체비중": curr_ratio,
                "권장비중": default_ratio,
                "과도여부": curr_ratio > default_ratio * 1.5,
            }

        return analysis

    def analyze(self, current_transactions, previous_transactions=None, total_budget=0, category_budgets=None):
        previous_transactions = previous_transactions or []
        category_budgets = category_budgets or {}

        current = self._aggregate(current_transactions)
        previous = self._aggregate(previous_transactions)

        remaining_budget = total_budget - current["total_spending"] if total_budget else 0

        weekly_spending = current["weekly_spending"]
        overspending_week = max(weekly_spending, key=weekly_spending.get) if weekly_spending else None

        current_weeks = [
            weekly_spending.get(w, 0)
            for w in ["1주차", "2주차", "3주차", "4주차", "5주차"]
            if w in weekly_spending
        ]

        if len(current_weeks) >= 2 and current_weeks[-2] > 0:
            spending_increase_rate = round(((current_weeks[-1] - current_weeks[-2]) / current_weeks[-2]) * 100, 1)
        else:
            spending_increase_rate = 0.0

        achievement_rate = round((current["total_spending"] / total_budget) * 100, 1) if total_budget > 0 else 0.0
        night_ratio = round((current["night_spending"] / current["total_spending"]) * 100, 1) if current["total_spending"] else 0.0

        impulse_score = self._compute_impulse_score(
            current["total_spending"],
            current["night_spending"],
            current["discretionary_spending"],
            current["weekend_spending"],
        )

        if impulse_score >= 70 or night_ratio >= 30:
            risk_level = "HIGH"
        elif impulse_score >= 40 or night_ratio >= 15:
            risk_level = "MEDIUM"
        else:
            risk_level = "LOW"

        category_budget_status = {}
        for category, budget in category_budgets.items():
            used = current["category_spending"].get(category, 0)
            remain = budget - used
            category_budget_status[category] = {
                "budget": budget,
                "used": used,
                "remaining": remain,
                "usageRate": round((used / budget) * 100, 1) if budget > 0 else 0.0
            }

        category_delta = self._category_delta(
            current["category_spending"],
            previous["category_spending"]
        )
        top_increase_category, top_decrease_category = self._find_top_change_categories(category_delta)

        return {
            "summary": {
                "totalBudget": total_budget,
                "totalSpending": current["total_spending"],
                "remainingBudget": remaining_budget,
                "achievementRate": achievement_rate,
                "dailyAverageSpending": current["daily_average_spending"],
                "weeklySpending": current["weekly_spending"],
                "overspendingWeek": overspending_week,
                "patternChange": self._build_pattern_change(current["weekly_spending"]),
            },
            "patterns": {
                "weekdaySpending": current["weekday_spending"],
                "hourSpending": current["hour_spending"],
                "topMerchants": current["top_merchants"],
            },
            "categories": {
                "categorySpending": current["category_spending"],
                "categoryRatio": current["category_ratio"],
                "weeklyCategorySpending": current["weekly_category_spending"],
                "categoryDeltaFromLastMonth": category_delta,
                "topCategories": current["top_categories"],
                "topIncreaseCategory": top_increase_category,
                "topDecreaseCategory": top_decrease_category,
                "categoryBudgetStatus": category_budget_status,
            },
            "risk": {
                "spendingIncreaseRate": spending_increase_rate,
                "nightSpendingRatio": night_ratio,
                "impulseScore": impulse_score,
                "riskLevel": risk_level,
            }
        }
