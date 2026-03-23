from app.clients.csv_adapter import load_transactions_from_csv
from app.services.spending_analysis_service import SpendingAnalysisService
from app.services.feedback_service import FeedbackService


def get_previous_month(year_month):
    from datetime import datetime

    dt = datetime.strptime(year_month, "%Y-%m")

    if dt.month == 1:
        return f"{dt.year - 1}-12"
    else:
        return f"{dt.year}-{dt.month - 1:02d}"


def main():
    current_month = "2025-02"
    previous_month = get_previous_month(current_month)

    print("CURRENT:", current_month)
    print("PREVIOUS:", previous_month)

    current_transactions = load_transactions_from_csv(
        "app/data/transaction.csv",
        user_id=1,
        year_month=current_month
    )

    previous_transactions = load_transactions_from_csv(
        "app/data/transaction.csv",
        user_id=1,
        year_month=previous_month
    )

    print("CURRENT COUNT:", len(current_transactions))
    print("PREVIOUS COUNT:", len(previous_transactions))

    analysis_service = SpendingAnalysisService()
    feedback_service = FeedbackService()

    report = analysis_service.analyze(
        current_transactions=current_transactions,
        previous_transactions=previous_transactions,
        total_budget=3000000,
        category_budgets={
            "food": 600000,
            "shopping": 500000,
            "fixed_expense": 800000,
            "transport": 300000,
            "living": 300000,
            "leisure": 300000
        }
    )

    feedback = feedback_service.generate_feedback(report)
    report["feedback"] = feedback["feedback"]

    print(report)


if __name__ == "__main__":
    main()