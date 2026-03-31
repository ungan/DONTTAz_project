# 플랜 생성 상세 명세

> `POST /plans/generate` 응답의 각 필드가 어떻게 계산되는지 설명합니다.

---

## 전체 처리 흐름

```
거래 내역 입력
    ↓
1. 카테고리 정규화 + NLP 재분류       (NormalizationService + NLPService)
2. 월소득 / 고정지출 추정             (BudgetEstimationService)
3. 향후 7일 지출 예측 (ML)            (ForecastService — PatchTST 모델)
4. 월급날까지 지출 예측 확장          (SealingService)
5. 봉인금액 / 사용 가능 예산 산정     (SealingService)
6. 카테고리별 예산 배분               (BudgetEstimationService)
7. 최종 risk_score 계산 (3피처 가중합) (RiskScorer)
8. 소비 추세 + 페르소나 + 요일 분석   (SpendingAnalysisService)
9. LLM 카테고리 코멘트 + 개인화 추천  (LLMRecommendationService)
10. LLM 실패 시 룰 기반 fallback     (RecommendationService)
```

---

## 응답 필드별 상세 설명

### `totalBudget` / `sealedAmount` / `surplusAmount`

```
currentBalance    = 요청으로 받은 현재 잔액
sealedAmount      = currentBalance - usableBudget  (봉인금액, 아래 참고)
totalBudget       = usableBudget                   (봉인 제외 사용 가능 예산)
surplusAmount     = max(월소득 - 고정지출 - totalBudget - sealedAmount, 0)
```

> `totalBudget`은 "이번달 자유롭게 써도 되는 금액"입니다.
> `surplusAmount`는 소득이 추정되는 경우에만 의미 있습니다.

---

### `sealedAmount` — 봉인금액 계산 상세

**목적:** 월급날까지 잔액이 고갈되지 않도록 현재 잔액 중 일부를 "봉인"해 건드리지 못하게 합니다.

#### Step 1. ML 모델로 향후 7일 일별 지출 예측

PatchTST(시계열 예측 모델)를 사용해 최근 카드 거래 내역 기반으로 다음 7일 일별 지출을 예측합니다.

#### Step 2. 월급날까지 예측 확장

처음 7일은 PatchTST 예측값을 사용하고, 7일 이후~월급날까지는 요일별 평균 지출(`weekdayAvg`)로 채워 월급날까지의 예측 총 지출(`predictedSpendUntilPayday`)을 구합니다.

> **예측 실패 시**: `weekdayAvg`가 있으면 전체 구간을 요일 평균으로 대체합니다. `weekdayAvg`도 없으면 0으로 채웁니다.

#### Step 3. 봉인금액 계산

```
preliminaryRisk = min(predictedSpendUntilPayday / (currentBalance + 1), 1.0)  ← ZeroDivision 방지

rollingStd      = 입력된 카드 거래 전 기간 일별 지출 시계열의 표준편차
alpha           = max(0.10, 1.5 × 과거MAE비율)  ← 과거 14일 rolling mean을 baseline으로 최근 7일 실제 지출과 비교한 MAE 비율 (데이터 14일 미만 시 0.15 고정)
riskBuffer      = alpha × predictedSpendUntilPayday + 1.2 × rollingStd
requiredBudget  = predictedSpendUntilPayday + riskBuffer

usableBudget    = clamp(requiredBudget, currentBalance × 0.2, currentBalance)
baseSealed      = currentBalance - usableBudget
sealedAmount    = min(baseSealed × (1 + 0.3 × preliminaryRisk), currentBalance × 0.8)
usableBudget    = currentBalance - sealedAmount
```

> `preliminaryRisk`는 봉인 계산 전용 임시 위험도입니다. 응답의 `riskScore`와 다릅니다 (아래 참고).

#### Step 4. 상한선 적용

`잔액의 80%` 상한은 항상 적용됩니다.
`처분가능소득의 30%` 상한은 **월 소득/고정지출 추정이 성공한 경우에만** 추가 적용됩니다.

```
# 항상 적용 (sealing.py 내부)
sealedAmount = min(sealedAmount, currentBalance × 0.8)

# 소득 추정 성공 시에만 추가 적용 (plan_usecase.py)
disposable   = max(월소득 - 고정지출, 0)
sealedAmount = min(sealedAmount, disposable × 0.3)
sealedAmount = min(sealedAmount, currentBalance × 0.8)
usableBudget = currentBalance - sealedAmount
```

---

### `riskScore` / `budgetStrategy`

카테고리 배분 완료 후 3개 피처의 가중합으로 계산됩니다.

```
predictedRatio         = min(predictedSpendUntilPayday / usableBudget, 1.0)
categoryPressureScore  = max(카테고리별 실제지출 / 배정예산)   ← 현재 급여 주기, 가장 초과한 카테고리 기준
burndownDeviationScore = clamp((이상적잔액 − 실제잔액) / usableBudget × 3)  ← 월초 5일간 완화 보정

riskScore = 0.5 × predictedRatio
          + 0.3 × categoryPressureScore
          + 0.2 × burndownDeviationScore
```

| `riskScore` 범위 | `budgetStrategy` |
|-----------------|-----------------|
| ≥ 0.8           | `tight`         |
| ≥ 0.5           | `balanced`      |
| < 0.5           | `safe`          |

> 봉인금액 계산 시 사용하는 `preliminaryRisk`(단순 예측지출/잔액 비율)와 다릅니다.
> 최종 `riskScore`는 예측 비율 외에 카테고리 압박과 번다운 이탈까지 반영합니다.

**사용처:**
1. `budgetStrategy` 결정
2. `LLMRecommendationService` 프롬프트에 전달 → 위험점수 0.8 이상이면 `warning` 타입 추천 포함
3. `RecommendationService` 룰 기반 fallback 분기

---

### `categoryPlans` — 카테고리별 예산 배분

카테고리별 예산은 다음 순서로 결정됩니다.

```
1. BudgetEstimationService.estimate_category_budgets()로 비율 산출
   - 카드 거래가 3건 이상: 실제 지출 비율로 배분
   - 카드 거래가 3건 미만: 통계청 기본 비율 사용 (아래 참고)

2. 각 카테고리 배정 금액 = (카테고리 비율 / 전체 비율 합계) × usableBudget

3. aiComment는 LLM이 채움 (실패 시 "최근 소비 패턴을 기반으로 예산을 배정했습니다.")
```

**통계청 기본 카테고리 비율 (2024 가계동향조사 기반):**

| 카테고리 | 기본 비율 |
|----------|---------|
| 식비 | 28% |
| 쇼핑 | 12% |
| 교통 | 11% |
| 생활 | 12% |
| 여가 | 8% |
| 의료/건강 | 8% |
| 교육 | 5% |
| 금융/보험 | 10% |
| 경조사/선물 | 4% |
| 기타 | 2% |

---

### `categoryPlans[].aiComment` — 카테고리 코멘트 (**LLM 생성**)

LLM(`gpt-4o-mini`)이 소비 추세 신호를 보고 카테고리별로 한 문장 조언을 생성합니다.

LLM에 전달되는 주요 신호:

| 신호 | 내용 |
|------|------|
| `핵심_소비_신호` | 권장 비중 초과/급증/감소/변화/패턴 요약 리스트 |
| `카테고리별_상세` | 이번달 / 전달 금액, 증감률, 과도여부 |
| `위험점수` | riskScore |
| `봉인금액` | sealedAmount |
| `월급일` | paydayDay |
| `월소득` | 추정 월소득 |
| `소비_페르소나` | 페르소나 + 주요 키워드 |
| `페르소나_변화` | 이전 페르소나 → 현재 페르소나 (변화 시) |
| `요일_패턴` | 지출 비중이 늘거나 준 요일 |

LLM 실패 시 모든 카테고리에 `"최근 소비 패턴을 기반으로 예산을 배정했습니다."` 로 대체됩니다.

---

### `personalizedRecommendations` — 개인화 추천 (**LLM 생성, 3~4개**)

LLM이 위 신호를 기반으로 `recommendations` 배열을 생성합니다.

| 타입 | 설명 |
|------|------|
| `warning` | 예산 초과 위험 경고 (riskScore ≥ 0.8 또는 과도 소비 카테고리) |
| `action` | 구체적 행동 조언 |
| `habit_change` | 소비 습관 개선 제안 (페르소나 변화 시 포함) |
| `saving_tip` | 절약 팁 |
| `positive` | 긍정적 소비 행동 칭찬 |

**LLM 실패 시 룰 기반 fallback (`RecommendationService`):**

```
1. [action] 예산이 가장 큰 카테고리 주간 한도 관리 제안
2. [habit_change] 식비/쇼핑이 1위면 해당 카테고리 절감 팁
3. [saving_tip] sealedAmount > 0이면 봉인금액 유지 추천
4. [warning/action/positive] riskScore에 따라 분기
   - ≥ 0.8: 선택 소비 최소화 warning
   - ≥ 0.5: 주간 점검 action
   - < 0.5: 예산 구조 안정 positive
```

---

### `spendingPersona` — 소비 페르소나

카테고리별 지출 비중을 분석해 소비 패턴을 도출합니다.
플랜 응답에도 포함되어 클라이언트가 사용자에게 바로 표시할 수 있습니다.

| 페르소나 | 주요 소비 카테고리 | 설명 |
|----------|--------------------|------|
| `식도락형` | 식비, 외식, 배달 | 음식 관련 지출이 집중된 패턴 |
| `쇼핑족` | 쇼핑 | 쇼핑 지출 비중이 높은 패턴 |
| `생활밀착형` | 교통, 생활 | 교통·생활 필수 지출 중심 패턴 |
| `여가충전형` | 여가 | 취미·엔터테인먼트 지출 비중이 높은 패턴 |
| `자기계발형` | 의료/건강, 교육 | 건강·교육 투자 중심 패턴 |
| `재무관리형` | 금융/보험 | 금융·보험 지출 비중이 높은 패턴 |
| `균형소비형` | (특정 카테고리 없음) | 특정 카테고리 집중 없이 고르게 소비 |

> 데이터가 부족하거나 분석이 불가능한 경우 `"데이터부족"` 또는 `"분석불가"` 문자열이 반환됩니다. 클라이언트는 이 두 값을 null과 동일하게 처리해야 합니다.

---

## 월소득 / 고정지출 추정 (`BudgetEstimationService`)

AI가 계좌 거래 내역에서 자동 추정합니다. 요청 스키마에 `monthlyIncome`, `fixedExpense` 필드가 존재하지만 **현재 구현에서는 사용되지 않으며**, 항상 거래 내역 기반 추정값이 우선 적용됩니다.

### 월소득 추정

```
1. 계좌 입금 중 10만원 이상 건을 후보로 추출
2. paydayDay가 주어진 경우: ±3일 이내 입금만 필터 (월말/월초 경계 처리 포함)
3. 금액 기준으로 내림차순 정렬
4. ±20% 내에서 2건 이상 반복되는 금액 → 중앙값을 월소득으로 결정
5. 반복 패턴 없으면 최대 단건 금액으로 fallback
```

### 고정지출 추정

```
계좌 출금 중 아래 키워드 포함 건 합산:
  "월세", "관리비", "보험", "통신", "자동이체", "정기결제",
  "카드대금", "전기", "가스", "수도", "이자", "원금상환"

카드 거래 중 "금융/보험" 카테고리 지출도 합산
```

### 총 예산 계산

```
총 예산 = max(월소득 - 고정지출 - 여유분(10%), 0)
```

---

## 카테고리 정규화 (`NormalizationService`)

Spring에서 전달된 카테고리명은 AI 서비스 내부 카테고리명으로 정규화됩니다.
SSAFY 금융망 카테고리명도 자동으로 매핑됩니다.

| Spring 전달값 | 내부 카테고리 |
|--------------|-------------|
| `food` | 식비 |
| `shopping` | 쇼핑 |
| `transport` | 교통 |
| `대형마트` | 생활 |
| `교육/육아` | 교육 |
| `통신` | 금융/보험 |
| `해외` | 쇼핑 |
| `생활` (SSAFY) | 기타 (NLP가 재분류) |

카테고리가 없거나 인식 불가한 경우 NLP 서비스가 가맹점명을 기반으로 자동 분류합니다.

---

## 카테고리 목록 (고정)

```
식비 / 쇼핑 / 교통 / 생활 / 여가 / 의료/건강 / 교육 / 금융/보험 / 경조사/선물 / 기타
```
