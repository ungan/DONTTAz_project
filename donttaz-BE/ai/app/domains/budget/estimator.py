from collections import defaultdict, Counter
import numpy as np


class BudgetEstimationService:
    FIXED_EXPENSE_KEYWORDS = [
        "월세", "관리비", "보험", "통신", "자동이체",
        "정기결제", "카드대금", "전기", "가스", "수도",
        "이자", "원금상환"
    ]

    # 통계청 2024 가계동향조사 기반 비율
    # 식비: 식료품(14.6%) + 외식(15.7%) → 20-30대 배달 소비 고려 상향
    # 의료/건강: 보건 비목 9.3% 반영 (기존 5%에서 상향)
    # 교통: 통계청 교통 비목 11.6% 반영
    # 생활: 주거·수도 중 고정지출 제외분 + 가정용품
    # 금융/보험: 통신(5%) + 보험료 합산
    # 교육: 전체 평균보다 낮게 (20-30대 자녀 교육비 적음, 자기계발 위주)
    DEFAULT_CATEGORY_RATIO = {
        "식비": 0.28,
        "쇼핑": 0.12,
        "교통": 0.11,
        "생활": 0.12,
        "여가": 0.08,
        "의료/건강": 0.08,
        "교육": 0.05,
        "금융/보험": 0.10,
        "경조사/선물": 0.04,
        "기타": 0.02,
    }

    def estimate_monthly_income(self, account_transactions: list[dict], payday_day: int = None) -> int:
        deposits = [
            tx for tx in account_transactions
            if tx.get("type") == "DEPOSIT" and int(tx.get("amount", 0)) >= 100_000
        ]

        if not deposits:
            return 0

        # payday_day가 주어지면 ±3일 이내 입금만 월급 후보로 필터
        if payday_day is not None:
            salary_candidates = []
            for tx in deposits:
                try:
                    day = int(str(tx.get("date", ""))[:10].split("-")[2])
                    if abs(day - payday_day) <= 3 or abs(day - payday_day) >= 28:  # 월말/월초 경계 처리
                        salary_candidates.append(tx)
                except (ValueError, IndexError):
                    pass
            if salary_candidates:
                deposits = salary_candidates

        amounts = sorted(
            [int(tx["amount"]) for tx in deposits],
            reverse=True,
        )

        # 반복성 검증: ±20% 내 금액이 2건 이상 묶이면 월급 후보
        for anchor in amounts:
            similar = [a for a in amounts if abs(a - anchor) / anchor <= 0.2]
            if len(similar) >= 2:
                return int(np.median(similar))

        # 반복 패턴 없으면 최대 단건으로 fallback
        return amounts[0]

    def estimate_payday(self, account_transactions: list[dict]) -> int:
        """월급 입금일(1~31)을 추정한다. 데이터 부족 시 25 반환."""
        income = self.estimate_monthly_income(account_transactions)
        if income == 0:
            return 25

        salary_days = []
        for tx in account_transactions:
            if tx.get("type") != "DEPOSIT":
                continue
            amount = int(tx.get("amount", 0))
            if amount <= 0:
                continue
            if abs(amount - income) / income > 0.2:
                continue
            try:
                day = int(str(tx.get("date", ""))[:10].split("-")[2])
                salary_days.append(day)
            except (ValueError, IndexError):
                pass

        if not salary_days:
            return 25

        return Counter(salary_days).most_common(1)[0][0]

    def estimate_fixed_expense(self, account_transactions: list[dict], card_transactions: list[dict]) -> int:
        fixed_total = 0

        # 계좌 출금 기반 최소 버전
        for tx in account_transactions:
            if tx.get("type") != "WITHDRAWAL":
                continue

            amount = int(tx.get("amount", 0))
            if amount <= 0:
                continue

            text = f"{tx.get('summary', '')} {tx.get('memo', '')}".strip()

            if any(keyword in text for keyword in self.FIXED_EXPENSE_KEYWORDS):
                fixed_total += amount

        # 카드 쪽에서 금융/보험 카테고리도 고정성 지출 후보로 일부 반영
        for tx in card_transactions:
            if tx.get("type") != "WITHDRAWAL":
                continue

            if tx.get("category") == "금융/보험":
                amount = int(tx.get("amount", 0))
                if amount > 0:
                    fixed_total += amount

        return fixed_total

    def estimate_total_budget(self, income: int, fixed_expense: int) -> int:
        if income <= 0:
            return 0

        buffer = int(income * 0.1)
        return max(income - fixed_expense - buffer, 0)

    def estimate_category_budgets(self, card_transactions: list[dict], total_budget: int) -> dict:
        expense_txs = [
            tx for tx in card_transactions
            if tx.get("type") == "WITHDRAWAL"
        ]

        if total_budget <= 0:
            return {}

        # 거래가 너무 적으면 기본 분배 사용
        if len(expense_txs) < 3:
            return {
                category: int(total_budget * ratio)
                for category, ratio in self.DEFAULT_CATEGORY_RATIO.items()
            }

        category_sum = defaultdict(int)
        total_spent = 0

        for tx in expense_txs:
            category = tx.get("category") or "기타"
            if category == "수입":
                continue

            amount = int(tx.get("amount", 0))
            if amount <= 0:
                continue

            category_sum[category] += amount
            total_spent += amount

        if total_spent == 0:
            return {
                category: int(total_budget * ratio)
                for category, ratio in self.DEFAULT_CATEGORY_RATIO.items()
            }

        budgets = {}
        for category, amount in category_sum.items():
            ratio = amount / total_spent
            budgets[category] = int(total_budget * ratio)

        # 혹시 특정 카테고리가 하나도 안 잡혀도 최소 기본 카테고리 틀은 유지하고 싶으면 아래 사용
        for category in self.DEFAULT_CATEGORY_RATIO.keys():
            budgets.setdefault(category, 0)

        return budgets

    def allocate_to_category_plans(self, category_budgets: dict, usable_budget: int, normalization_service) -> list:
        """카테고리별 예산 비율을 usable_budget에 맞게 재배분하여 category_plans 리스트 반환."""
        raw_sum = sum(category_budgets.values())
        plans = []
        for category, amount in category_budgets.items():
            allocated = round((amount / raw_sum) * usable_budget) if raw_sum > 0 else 0
            if allocated <= 0:
                continue
            plans.append({
                "categoryName": normalization_service.to_service_category(category),
                "allocatedAmount": allocated,
                "aiComment": "",
            })
        plans.sort(key=lambda x: x["allocatedAmount"], reverse=True)
        return plans

    def estimate(self, current_account_transactions: list[dict], current_card_transactions: list[dict], payday_day: int = None) -> dict:
        income = self.estimate_monthly_income(current_account_transactions, payday_day=payday_day)
        fixed_expense = self.estimate_fixed_expense(
            current_account_transactions,
            current_card_transactions
        )
        total_budget = self.estimate_total_budget(income, fixed_expense)
        category_budgets = self.estimate_category_budgets(
            current_card_transactions,
            total_budget
        )

        return {
            "monthlyIncome": income,
            "fixedExpense": fixed_expense,
            "totalBudget": total_budget,
            "categoryBudgets": category_budgets,
        }
