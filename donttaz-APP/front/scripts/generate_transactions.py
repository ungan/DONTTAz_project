"""
거래내역 더미 데이터 생성 스크립트 v2
- user_id: 2, account_id: 1
- 시작잔액: 5,000,000원
- 월급: 3,500,000원 (매월 25일)
- 기간: 2023-01-01 ~ 2026-03-20

반영된 로직:
  1. 월별 소비 성향 프로파일 (1월 절약, 12월 지출 폭발 등)
  3. 급여일 전후 패턴 (1~15일 정상 / 16~24일 절약 / 25~27일 지출 폭발)
  4. 특정 이벤트 (설/추석 전 대형마트, 12월 회식)
  5. 한국 20~30대 소비 카테고리 비율 반영
  6. 교통 패턴: 평일 출퇴근 지하철 각각 별도 거래, 가끔 아침 택시
  7. ATM 출금 포함 (주 1회 정도)
"""

import random
from datetime import datetime, timedelta

# ── 기본 설정 ──────────────────────────────────────────
USER_ID       = 2
ACCOUNT_ID    = 5
SOURCE        = "FIN_API"
START_DATE    = datetime(2023, 1, 1)
END_DATE      = datetime(2026, 3, 20)
INITIAL_BAL   = 5_000_000
SALARY        = 3_500_000
SALARY_DAY    = 25
OUTPUT_FILE   = "transactions.sql"
MIN_BAL       = 10_000       # 최소 유지 잔액

random.seed(42)

# ── 1. 월별 소비 배율 ──────────────────────────────────
MONTHLY_PROFILE = {
    1: 0.72,   # 신년 절약 모드
    2: 0.82,   # 여전히 절약
    3: 1.20,   # 봄 세일 / 신학기
    4: 0.90,   # 평범
    5: 1.10,   # 가정의 달 (어버이날)
    6: 1.15,   # 여름 세일
    7: 1.00,   # 여름
    8: 1.05,   # 여름 막바지
    9: 0.87,   # 평범
    10: 0.92,  # 평범
    11: 1.32,  # 블랙프라이데이
    12: 1.48,  # 연말 / 크리스마스
}

# ── 3. 급여일 전후 배율 ────────────────────────────────
def pay_period_factor(day):
    if day <= 15:   return 1.00   # 월 초반: 정상
    if day <= 24:   return 0.72   # 월급 전: 절약
    if day <= 27:   return 1.38   # 월급 직후: 지출 폭발
    return 1.12                   # 월말: 약간 높음

# ── 4. 설날 / 추석 날짜 ────────────────────────────────
SEOL_DATES = [
    datetime(2023, 1, 22),
    datetime(2024, 2, 10),
    datetime(2025, 1, 29),
    datetime(2026, 2, 17),
]
CHUSEOK_DATES = [
    datetime(2023, 9, 29),
    datetime(2024, 9, 17),
    datetime(2025, 10, 6),
]

def holiday_prep_type(date):
    """설/추석 당일~3일 전이면 이름 반환, 아니면 None"""
    for hd in SEOL_DATES:
        if 0 <= (hd - date).days <= 3:
            return "설날"
    for hd in CHUSEOK_DATES:
        if 0 <= (hd - date).days <= 3:
            return "추석"
    return None

# ── 계절별 공과금 ──────────────────────────────────────
def electricity_bill(month):
    if month in (7, 8):        return random.randint(78000, 135000)
    if month in (12, 1, 2):    return random.randint(58000, 98000)
    return random.randint(28000, 58000)

def gas_bill(month):
    if month in (12, 1, 2):    return random.randint(68000, 125000)
    if month in (3, 11):       return random.randint(32000, 68000)
    return random.randint(5000, 22000)

# ── 세일 기간 배율 ─────────────────────────────────────
def sale_mult(date):
    m, d = date.month, date.day
    if m == 3  and d <= 20:        return 2.5
    if m == 6  and 15 <= d <= 30:  return 2.0
    if m == 11 and 20 <= d <= 30:  return 3.0
    if m == 12 and d <= 25:        return 2.5
    return 1.0

# ── 가맹점 테이블: (이름, 은행코드, (최소금액, 최대금액)) ──
MERCHANTS = {
    "카페": [
        ("스타벅스",      "088", (5500, 7500)),
        ("투썸플레이스",  "088", (5000, 7000)),
        ("이디야커피",    "088", (3500, 5000)),
        ("메가커피",      "088", (2000, 3500)),
        ("컴포즈커피",    "088", (2000, 3000)),
        ("빽다방",        "088", (2500, 4000)),
    ],
    "편의점": [
        ("CU편의점",   "023", (1500, 9500)),
        ("GS25",       "023", (1500, 9000)),
        ("세븐일레븐", "023", (1500, 8000)),
        ("이마트24",   "088", (1500, 9000)),
    ],
    "점심": [
        ("한솥도시락",      "088", (6000, 9000)),
        ("본죽",            "088", (7000, 10000)),
        ("김밥천국",        "088", (5000, 8500)),
        ("롯데리아",        "088", (7000, 12000)),
        ("맥도날드",        "088", (8000, 14000)),
        ("서브웨이",        "088", (8000, 12500)),
        ("버거킹",          "088", (8000, 14000)),
        ("샐러드집",        "088", (8000, 13000)),
        ("편의점도시락",    "023", (4000, 7000)),
        ("국밥집",          "088", (7000, 10000)),
        ("순대국",          "088", (7000, 9000)),
        ("돼지국밥",        "088", (8000, 10000)),
    ],
    "저녁": [
        ("홍콩반점",    "088", (10000, 18000)),
        ("BBQ치킨",     "088", (20000, 28000)),
        ("교촌치킨",    "088", (19000, 26000)),
        ("피자헛",      "088", (22000, 38000)),
        ("도미노피자",  "088", (20000, 33000)),
        ("삼겹살집",    "088", (20000, 42000)),
        ("일식집",      "088", (18000, 35000)),
        ("중국집",      "088", (12000, 22000)),
        ("분식집",      "088", (8000, 15000)),
    ],
    "야식": [
        ("배달의민족",  "999", (16000, 38000)),
        ("요기요",      "999", (14000, 32000)),
        ("쿠팡이츠",    "999", (15000, 35000)),
    ],
    "마트": [
        ("이마트",      "088", (25000, 90000)),
        ("홈플러스",    "088", (20000, 75000)),
        ("롯데마트",    "088", (20000, 70000)),
        ("코스트코",    "088", (60000, 220000)),
        ("GS더프레시",  "023", (15000, 50000)),
    ],
    "쇼핑": [
        ("쿠팡",        "999", (12000, 85000)),
        ("무신사",      "999", (35000, 130000)),
        ("올리브영",    "088", (15000, 65000)),
        ("다이소",      "088", (3000, 22000)),
        ("유니클로",    "088", (25000, 90000)),
        ("H&M",         "088", (20000, 75000)),
        ("자라",        "088", (35000, 130000)),
        ("지그재그",    "999", (25000, 95000)),
        ("에이블리",    "999", (20000, 80000)),
        ("11번가",      "999", (10000, 70000)),
    ],
    "의료": [
        ("동네의원",        "088", (5000, 15000)),
        ("약국",            "088", (3000, 22000)),
        ("치과",            "088", (15000, 80000)),
        ("피부과",          "088", (20000, 80000)),
        ("세브란스병원",    "088", (10000, 80000)),
    ],
    "문화": [
        ("CGV",             "088", (12000, 20000)),
        ("롯데시네마",      "088", (12000, 20000)),
        ("볼링장",          "088", (8000, 15000)),
        ("당구장",          "088", (6000, 12000)),
    ],
    # 4. 12월 회식용
    "회식": [
        ("이자카야",        "088", (35000, 75000)),
        ("고깃집",          "088", (30000, 62000)),
        ("회식집",          "088", (25000, 55000)),
        ("노래방",          "088", (15000, 32000)),
    ],
    "지하철": [
        ("서울시교통공사",  "020", (1250, 1500)),
        ("티머니",          "020", (1250, 1500)),
    ],
    "택시": [
        ("카카오택시",      "999", (4000, 18000)),
        ("타다",            "999", (5000, 22000)),
    ],
    "교통카드충전": [
        ("티머니충전",      "020", None),
        ("캐시비충전",      "020", None),
    ],
}

# 고정 월 지출
RECURRING = [
    # (일, 이름, 카테고리, (최소,최대), 은행코드)
    (1,  "SKT",           "통신",   (82000,  95000),  "011"),
    (5,  "넷플릭스",       "구독",   (17000,  17000),  "999"),
    (5,  "유튜브프리미엄", "구독",   (14900,  14900),  "999"),
    (7,  "쿠팡로켓와우",   "구독",   (7890,   7890),   "999"),
    (10, "삼성생명",       "보험",   (115000, 125000), "088"),
    (15, "한국전력공사",   "공과금", (0,      0),      "020"),  # 계절별 별도 계산
    (15, "한국가스공사",   "공과금", (0,      0),      "020"),  # 계절별 별도 계산
    (20, "서울시수도사업", "공과금", (12000,  18000),  "020"),
]

# ── 거래 목록 & 잔액 ───────────────────────────────────
transactions = []
tx_counter   = 1
balance      = INITIAL_BAL
transit_bal  = 30000   # 교통카드 잔액 (별도 추적)

def add_tx(dt, tx_type, amount, name, category, bank="088",
           account_no=None, summary=None):
    global balance, tx_counter
    if amount <= 0:
        return
    if tx_type == "WITHDRAWAL":
        if balance - amount < MIN_BAL:
            return
        balance -= amount
    else:
        balance += amount

    transactions.append({
        "account_id":          ACCOUNT_ID,
        "user_id":             USER_ID,
        "source":              SOURCE,
        "source_tx_id":        f"TX{tx_counter:08d}",
        "tx_type":             tx_type,
        "tx_type_name":        "입금" if tx_type == "DEPOSIT" else "출금",
        "amount":              amount,
        "balance_snapshot":    balance,
        "counterparty_name":   name,
        "ai_category":         category,
        "counterparty_bank":   bank,
        "counterparty_account_no": account_no or f"{random.randint(10000000000, 99999999999):011d}",
        "transaction_summary": summary or name,
        "transaction_at":      dt.strftime("%Y-%m-%d %H:%M:%S"),
        "synced_at":           (dt + timedelta(minutes=random.randint(1, 30))).strftime("%Y-%m-%d %H:%M:%S"),
    })
    tx_counter += 1

def pick(lst):
    return random.choice(lst)

def rand_time(base_dt, hour_lo, hour_hi):
    return base_dt.replace(
        hour=random.randint(hour_lo, hour_hi),
        minute=random.randint(0, 59),
        second=random.randint(0, 59),
    )

# ── 날짜별 생성 ────────────────────────────────────────
cur = START_DATE
while cur <= END_DATE:
    m       = cur.month
    d       = cur.day
    wd      = cur.weekday()   # 0=월 … 6=일
    is_work = wd < 5
    is_fri  = wd == 4
    is_wknd = wd >= 5

    mp      = MONTHLY_PROFILE.get(m, 1.0)
    pp      = pay_period_factor(d)
    combo   = mp * pp          # 종합 소비 배율
    sm      = sale_mult(cur)   # 세일 배율
    hp      = holiday_prep_type(cur)

    # ── 월급 (25일) ─────────────────────────────────────
    if d == SALARY_DAY:
        add_tx(rand_time(cur, 9, 9), "DEPOSIT", SALARY,
               "주식회사돈타즈", "급여", "088", summary="급여")

    # ── 고정 월 지출 ────────────────────────────────────
    for (day, name, cat, amt_range, bank) in RECURRING:
        if d != day:
            continue
        if name == "한국전력공사":
            amount = electricity_bill(m)
        elif name == "한국가스공사":
            amount = gas_bill(m)
        else:
            amount = random.randint(*amt_range)
        add_tx(rand_time(cur, 9, 11), "WITHDRAWAL", amount, name, cat, bank)

    # ── 6. 교통: 평일 출퇴근 지하철 ────────────────────
    if is_work:
        # 출근
        fare1 = random.randint(1250, 1500)
        transit_bal -= fare1
        subway = pick(MERCHANTS["지하철"])
        add_tx(rand_time(cur, 7, 9), "WITHDRAWAL", fare1,
               subway[0], "교통", subway[1], summary="교통카드(출근)")

        # 퇴근
        fare2 = random.randint(1250, 1500)
        transit_bal -= fare2
        subway2 = pick(MERCHANTS["지하철"])
        add_tx(rand_time(cur, 18, 20), "WITHDRAWAL", fare2,
               subway2[0], "교통", subway2[1], summary="교통카드(퇴근)")

        # 교통카드 잔액 부족 → 충전
        if transit_bal < 5000:
            charge_amt = random.choice([30000, 50000])
            transit_bal += charge_amt
            charger = pick(MERCHANTS["교통카드충전"])
            add_tx(rand_time(cur, 7, 9), "WITHDRAWAL", charge_amt,
                   charger[0], "교통", charger[1], summary="교통카드충전")

        # 아침 택시 (늦잠/비): 8% 확률
        if random.random() < 0.08:
            cab = pick(MERCHANTS["택시"])
            add_tx(rand_time(cur, 8, 9), "WITHDRAWAL",
                   random.randint(*cab[2]), cab[0], "교통", cab[1])

    # ── 카페 ────────────────────────────────────────────
    cafe_prob = 0.75 if is_work else 0.42
    if random.random() < cafe_prob:
        c = pick(MERCHANTS["카페"])
        add_tx(rand_time(cur, 8, 10), "WITHDRAWAL",
               random.randint(*c[2]), c[0], "카페", c[1])

    # ── 점심 ────────────────────────────────────────────
    if is_work:
        if random.random() < 0.88:
            r = pick(MERCHANTS["점심"])
            amt = int(random.randint(*r[2]) * combo)
            add_tx(rand_time(cur, 11, 13), "WITHDRAWAL", amt, r[0], "식비", r[1])
    else:  # 주말 브런치/점심
        if random.random() < 0.52:
            r = pick(MERCHANTS["저녁"])   # 주말 점심은 좀 더 무거운 식사
            amt = int(random.randint(*r[2]) * combo)
            add_tx(rand_time(cur, 12, 14), "WITHDRAWAL", amt, r[0], "식비", r[1])

    # ── 저녁 ────────────────────────────────────────────
    dinner_prob = 0.40 if is_work else 0.65
    if random.random() < dinner_prob:
        # 4. 12월 회식 비율 40%로 증가
        if m == 12 and random.random() < 0.40:
            r = pick(MERCHANTS["회식"])
        else:
            r = pick(MERCHANTS["저녁"])
        amt = int(random.randint(*r[2]) * combo)
        add_tx(rand_time(cur, 18, 21), "WITHDRAWAL", amt, r[0], "식비", r[1])

    # ── 야식 ────────────────────────────────────────────
    # 금요일 밤 70%, 주말 22%, 평일 10%
    if is_fri:      yp = 0.72
    elif is_wknd:   yp = 0.22
    else:           yp = 0.10
    if random.random() < yp:
        y = pick(MERCHANTS["야식"])
        add_tx(rand_time(cur, 21, 23), "WITHDRAWAL",
               random.randint(*y[2]), y[0], "야식", y[1])

    # ── 편의점 ──────────────────────────────────────────
    if random.random() < 0.45:
        cv = pick(MERCHANTS["편의점"])
        add_tx(rand_time(cur, 7, 23), "WITHDRAWAL",
               random.randint(*cv[2]), cv[0], "편의점", cv[1])

    # ── 마트 ────────────────────────────────────────────
    # 4. 설/추석 준비 기간: 마트 방문 확률 폭발 + 금액 2.5배
    if hp:
        if random.random() < 0.78:
            mk = pick(MERCHANTS["마트"])
            amt = int(random.randint(*mk[2]) * 2.5)
            add_tx(rand_time(cur, 14, 19), "WITHDRAWAL", amt,
                   mk[0], "마트", mk[1], summary=f"{hp}준비")
    else:
        mart_prob = 0.12 if is_work else 0.32
        if random.random() < mart_prob * mp:
            mk = pick(MERCHANTS["마트"])
            add_tx(rand_time(cur, 14, 19), "WITHDRAWAL",
                   random.randint(*mk[2]), mk[0], "마트", mk[1])

    # ── 쇼핑 ────────────────────────────────────────────
    # 세일 기간엔 확률·금액 모두 상승
    shop_prob = 0.09 * sm * mp
    if random.random() < shop_prob:
        sk = pick(MERCHANTS["쇼핑"])
        amt = int(random.randint(*sk[2]) * (sm ** 0.5))
        add_tx(rand_time(cur, 10, 22), "WITHDRAWAL", amt, sk[0], "쇼핑", sk[1])

    # ── 의료 ────────────────────────────────────────────
    if random.random() < 0.05:
        med = pick(MERCHANTS["의료"])
        add_tx(rand_time(cur, 10, 17), "WITHDRAWAL",
               random.randint(*med[2]), med[0], "의료", med[1])

    # ── 문화 (주말) ─────────────────────────────────────
    if is_wknd and random.random() < 0.14 * mp:
        cult = pick(MERCHANTS["문화"])
        add_tx(rand_time(cur, 13, 20), "WITHDRAWAL",
               random.randint(*cult[2]), cult[0], "문화", cult[1])

    # ── 7. ATM 출금 (주 1회 정도, 14% 확률) ────────────
    if random.random() < 0.14:
        atm_amt = random.choice([30000, 50000, 50000, 100000])
        add_tx(rand_time(cur, 10, 20), "WITHDRAWAL", atm_amt,
               "ATM출금", "현금출금", "088",
               account_no="ATM00000000", summary="현금출금")

    cur += timedelta(days=1)

# ── SQL 파일 생성 ──────────────────────────────────────
def esc(v):
    return str(v).replace("'", "''")

COLS = (
    "account_id, user_id, source, source_tx_id, tx_type, tx_type_name, "
    "amount, balance_snapshot, counterparty_name, ai_category, "
    "counterparty_bank, counterparty_account_no, transaction_summary, "
    "transaction_at, synced_at, created_at, updated_at, is_deleted"
)

header = f"SET NAMES utf8mb4;\nSET time_zone = '+09:00';\n\nINSERT INTO account_transaction\n  ({COLS})\nVALUES\n"

rows = []
for t in transactions:
    rows.append(
        f"  ({t['account_id']}, {t['user_id']}, "
        f"'{esc(t['source'])}', '{esc(t['source_tx_id'])}', "
        f"'{t['tx_type']}', '{t['tx_type_name']}', "
        f"{t['amount']}, {t['balance_snapshot']}, "
        f"'{esc(t['counterparty_name'])}', '{esc(t['ai_category'])}', "
        f"'{esc(t['counterparty_bank'])}', '{esc(t['counterparty_account_no'])}', "
        f"'{esc(t['transaction_summary'])}', "
        f"'{t['transaction_at']}', '{t['synced_at']}', "
        f"NOW(), NOW(), 0)"
    )

with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
    f.write(header)
    f.write(",\n".join(rows))
    f.write(";\n")

print(f"생성 완료: {len(transactions):,}건 → {OUTPUT_FILE}")
print(f"최종 잔액: {balance:,}원")
