def format_datetime(date_str: str, time_str: str) -> str:
    return (
        f"{date_str[0:4]}-{date_str[4:6]}-{date_str[6:8]}T"
        f"{time_str[0:2]}:{time_str[2:4]}:{time_str[4:6]}"
    )


# SSAFY 카드 카테고리 -> 표준 카테고리
CARD_CATEGORY_MAP = {
    "주유": "교통",
    "택시": "교통",
    "버스": "교통",
    "지하철": "교통",
    "대중교통": "교통",
    "주차": "교통",

    "카페": "식비",
    "배달": "식비",
    "외식": "식비",
    "식당": "식비",

    "마트": "생활",
    "편의점": "생활",
    "생필품": "생활",

    "병원": "의료/건강",
    "약국": "의료/건강",
    "의료": "의료/건강",
    "건강": "의료/건강",

    "보험": "금융/보험",
    "이자": "금융/보험",
    "카드대금": "금융/보험",
    "원금상환": "금융/보험",

    "영화": "여가",
    "게임": "여가",
    "여행": "여가",
    "도서": "여가",

    "학원": "교육",
    "응시료": "교육",
    "수강료": "교육",

    "선물": "경조사/선물",
    "축의금": "경조사/선물",
    "조의금": "경조사/선물",
    "기프티콘": "경조사/선물",
}


def normalize_card_category(category: str | None) -> str:
    if not category:
        return "기타"
    category = str(category).strip()
    return CARD_CATEGORY_MAP.get(category, category)


def map_ssafy_card_transaction(tx: dict, estimated_balance: str) -> dict | None:
    try:
        amount = int(tx["transactionBalance"])
    except (KeyError, TypeError, ValueError):
        return None

    if amount <= 0:
        return None

    raw_category = tx.get("categoryName", "기타")
    normalized_category = normalize_card_category(raw_category)

    return {
        "transaction_id": tx.get("transactionUniqueNo"),
        "date": format_datetime(tx["transactionDate"], tx["transactionTime"]),
        "amount": amount,
        "merchant": tx.get("merchantName", ""),
        "category": normalized_category,
        "raw_category": raw_category,   # 원본 카테고리도 남겨두면 디버깅 편함
        "type": "WITHDRAWAL",
        "balance": int(estimated_balance),
        "summary": None,
        "memo": None,
    }


def map_ssafy_card_response(response_json: dict) -> list[dict]:
    rec = response_json.get("REC", {})
    estimated_balance = rec.get("estimatedBalance", "0")
    transaction_list = rec.get("transactionList", [])

    results = []
    for tx in transaction_list:
        if tx.get("cardStatus") != "승인":
            continue
        item = map_ssafy_card_transaction(tx, estimated_balance)
        if item is not None:
            results.append(item)
    return results


def map_ssafy_account_transaction(tx: dict) -> dict | None:
    type_name = tx.get("transactionTypeName", "")

    if "입금" in type_name:
        tx_type = "DEPOSIT"
    elif "출금" in type_name:
        tx_type = "WITHDRAWAL"
    else:
        return None

    return {
        "transaction_id": tx.get("transactionUniqueNo"),
        "date": format_datetime(tx["transactionDate"], tx["transactionTime"]),
        "amount": int(tx["transactionBalance"]),
        "merchant": None,
        "category": None,
        "raw_category": None,
        "type": tx_type,
        "balance": int(tx.get("transactionAfterBalance", 0)),
        "summary": tx.get("transactionSummary"),
        "memo": tx.get("transactionMemo"),
        "raw_type_name": tx.get("transactionTypeName"),
    }


def map_ssafy_account_response(response_json: dict) -> list[dict]:
    rec = response_json.get("REC", {})
    tx_list = rec.get("list", [])

    results = []
    for tx in tx_list:
        item = map_ssafy_account_transaction(tx)
        if item is not None:
            results.append(item)
    return results