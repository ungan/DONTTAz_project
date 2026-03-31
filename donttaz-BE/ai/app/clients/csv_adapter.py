import pandas as pd


def map_row_to_transaction(row):
    category = str(row["main_category"]).lower()
    tx_type = "DEPOSIT" if category == "income" else "WITHDRAWAL"

    return {
        "date": str(row["datetime"]),
        "amount": int(row["amount"]),
        "merchant": row["merchant_name"],
        "category": row["main_category"],
        "type": tx_type,
        "balance": int(row["balance_after_tx"]),
    }


def load_transactions_from_csv(file_path, user_id=None, year_month=None):
    df = pd.read_csv(file_path)

    if user_id is not None:
        df = df[df["user_id"] == user_id]

    if year_month is not None:
        df["datetime"] = pd.to_datetime(df["datetime"])
        df = df[df["datetime"].dt.strftime("%Y-%m") == year_month]

    transactions = []
    for _, row in df.iterrows():
        transactions.append(map_row_to_transaction(row))

    return transactions