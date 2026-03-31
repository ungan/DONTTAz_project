# AI 서비스 기술 문서

> FastAPI 기반 AI 분석 서버. Spring BE에서 HTTP 요청을 받아 예산 플랜, 소비 리포트, 번다운 차트, 카테고리 분류 결과를 반환합니다.

---

## 전체 구조

```
Spring BE  →  AI 서버 (FastAPI)  →  응답 반환
                    ↓
         ┌──────────────────────┐
         │  NLP 카테고리 분류    │  가맹점명 → 카테고리
         │  소비 예측 (PatchTST) │  다음 7일 지출 예측
         │  봉인금액 계산        │  월급일까지 안전 예산 산정
         │  소비 패턴 분석       │  전달 비교, 페르소나, 요일 패턴
         │  LLM 추천 생성        │  GPT-4o-mini 개인화 조언
         └──────────────────────┘
```

---

## API 엔드포인트

### 1. `POST /plans/generate` — 다음달 예산 플랜 생성

**입력**: 카드 거래 내역, 계좌 거래 내역, 현재 잔액, 월급일

**처리 흐름**:

```
1. 카테고리 정규화 (SSAFY 카테고리 → 한글)
2. NLP 재분류 (카테고리 "기타"인 거래를 가맹점명으로 재분류)
3. 월 소득 / 고정지출 추정 (계좌 입금 패턴 분석)
4. 다음 7일 지출 예측 (PatchTST 글로벌 모델)
5. 봉인금액 계산 (예측 지출 + 리스크 버퍼)
6. 카테고리별 예산 배분 (과거 지출 비율 기반)
7. 소비 분석 (전달 비교, 요일 패턴, 페르소나 변화 감지)
8. LLM 개인화 추천 생성 (GPT-4o-mini)
```

**출력 주요 필드**:

| 필드 | 설명 |
|------|------|
| `totalBudget` | 사용 가능 예산 (잔액 - 봉인금액) |
| `sealedAmount` | 봉인금액 (월급일까지 지출 예비비) |
| `riskScore` | 위험도 0.0~1.0 (예측비율·카테고리압박·번다운이탈 가중합) |
| `budgetStrategy` | `safe` / `balanced` / `tight` |
| `categoryPlans` | 카테고리별 배정 금액 + AI 코멘트 |
| `personalizedRecommendations` | 개인화 추천 3~4개 |
| `spendingPersona` | 소비 페르소나 (식도락형/쇼핑족 등) |

---

### 2. `POST /reports/generate` — 월 소비 리포트 생성

**입력**: 카드/계좌 거래 내역, 해당 월, 플랜 정보(선택)

**처리 흐름**:

```
1. 거래 정규화
2. 카테고리별 예산 달성률 계산
3. 주차별 소비 패턴 분석 (1주차~5주차)
4. 충동소비 지수 산정 (야간 + 주말 + 재량지출 비율)
5. LLM 전체 피드백 생성, 실패 시 룰 기반 fallback
```

**출력 주요 필드**:

| 필드 | 설명 |
|------|------|
| `achievementRate` | 예산 달성률 (%) |
| `aiOverallFeedback` | 전체 종합 피드백 문장 |
| `categoryReports` | 카테고리별 지출/예산/달성률 |
| `personalizedFeedback` | 개인화 피드백 목록 |

---

### 3. `POST /burndown/calculate` — 번다운 차트 계산

**입력**: 카드 거래 내역, 현재 잔액, 플랜 정보(선택)

**처리 흐름**:

```
1. 직전 월급일 ~ 다음 월급일 범위 설정
2. 날짜별 실제 지출 집계
3. 오늘 이후 날짜는 PatchTST 예측값으로 채움
4. 이상적 선(선형), 실제 선, 예측 선 3개 생성
5. 잔액 소진 예상일(D-day) 계산
```

**출력 주요 필드**:

| 필드 | 설명 |
|------|------|
| `chartData` | 날짜별 `idealRemaining` / `actualRemaining` / `predictedRemaining` |
| `zeroDate` | 잔액 소진 예상일 |
| `daysToZero` | 소진까지 남은 일수 |
| `isSurviving` | 월급일까지 버티는지 여부 |

---

### 4. `POST /categories/classify` — 거래 카테고리 분류

**입력**: 거래 목록 (가맹점명, 금액, 기존 카테고리)

**처리 흐름**:

```
1. 기존 카테고리 있으면 그대로 사용
2. 없거나 "기타"면 가맹점명으로 분류 시도
   - 1차: 키워드 규칙 매칭 (정확도 0.90~0.98)
   - 2차: SBERT 한국어 임베딩 유사도 (jhgan/ko-sbert-multitask)
   - 3차: 기타 fallback (정확도 0.35)
```

**`POST /categories/classify/ssafy`**: SSAFY 금융망 원본 응답을 그대로 전달하면 파싱 후 분류 결과 반환

**`PATCH /categories`**: 카테고리명 정규화 후 반환. AI 서버 내부에 영속 저장하지 않으며, Spring BE(DB)가 결과를 저장하고 이후 요청 시 수정된 카테고리를 함께 전달해야 NLP가 덮어쓰지 않음

---

## 핵심 서비스 모듈

### 봉인금액 계산 (`SealingService`)

> "월급일까지 쓸 돈은 미리 빼두고, 나머지만 자유롭게 쓰세요"

```
preliminaryRisk = min(예측지출 / 잔액, 1.0)

riskBuffer      = (alpha × 예측지출) + (beta × 최근지출_표준편차)
usableBudget    = clamp(예측지출 + riskBuffer, 잔액 × 0.2, 잔액)
baseSealed      = 잔액 - usableBudget
봉인금액         = min(baseSealed × (1 + gamma × preliminaryRisk), 잔액 × 0.8)
```

- `alpha`: `max(0.10, 1.5 × MAE비율)` — 과거 14일 rolling mean을 baseline으로 최근 7일 실제 지출과 비교한 MAE 비율 기반 동적 계산 (데이터 14일 미만 시 0.15 고정)
- `beta`: 1.2 고정 — 입력 카드 거래 전 기간 일별 지출 표준편차 증폭 계수
- `gamma`: 0.3 고정 — preliminaryRisk 기반 봉인금액 추가 증폭 계수 (최대 +30%)
- 상한선: 기본적으로 `잔액의 80%`. **월 소득/고정지출이 추정되는 경우에 한해** `처분가능소득의 30%` 상한 추가 적용

---

### 소비 예측 (`ForecastService`)

- 사전학습된 **PatchTST 글로벌 모델** 사용 (`models/patchtst_global.pkl`)
- 일별 지출 시계열로 변환 후 다음 7일 예측
- 예측 실패 시 빈 배열 반환 → SealingService가 요일 평균(`weekdayAvg`)으로 전체 구간 대체. 요일 평균도 없으면 0으로 처리

---

### 카테고리 NLP 분류 (`NLPService`)

- 서버 시작 시 SBERT 모델 1회 로딩 (요청마다 재로딩 없음)
- 분류 우선순위: 기존 카테고리 → 키워드 규칙 → SBERT 임베딩
- 플랜 생성 시 카드 거래에 자동 적용됨

---

### 소비 분석 (`SpendingAnalysisService`)

| 메서드 | 설명 |
|--------|------|
| `analyze_spending_trends()` | 카테고리별 전달 대비 증감률, 권장 비중 초과 여부 |
| `extract_spending_persona()` | 카테고리 지출 비중 기반 소비 페르소나 도출 |
| `analyze_weekday_pattern()` | 요일별 지출 비중 전달 대비 변화 |
| `analyze_persona_shift()` | 월별 페르소나 변화 감지 |
| `analyze()` | 리포트용 종합 분석 (주차별, 충동지수, 위험도) |

**소비 페르소나 종류**: 식도락형 / 쇼핑족 / 생활밀착형 / 여가충전형 / 자기계발형 / 재무관리형 / 균형소비형

---

### LLM 추천 생성 (`LLMRecommendationService`)

- 모델: `gpt-4o-mini` (SSAFY GMS API)
- LLM에 전달하는 핵심 신호 (코드에서 미리 요약):

```
[경고] 식비 권장 비중 초과 (+45%) — 이번달 350,000원 / 전달 240,000원
[변화] 소비 성향 생활밀착형 → 쇼핑족
[패턴] 토요일 지출 비중 +15%p 집중
```

- LLM 실패 시 룰 기반 `RecommendationService`로 자동 fallback

---

## Spring BE 연동 시 참고사항

- **카테고리명**: 한글 고정 (`식비`, `쇼핑`, `교통`, `생활`, `여가`, `의료/건강`, `교육`, `금융/보험`, `경조사/선물`, `기타`)
- **거래 날짜 형식**: `YYYY-MM-DD` (ISO 8601)
- **카테고리 수정**: 사용자가 앱에서 카테고리를 수정하면 Spring이 저장 → AI 재요청 시 수정된 카테고리로 전달하면 NLP가 덮어쓰지 않음
- **모든 금액**: 원(₩) 단위 정수

---

## 주요 의존성

| 라이브러리 | 용도 |
|-----------|------|
| `fastapi` | API 서버 |
| `neuralforecast` | PatchTST 시계열 예측 |
| `sentence-transformers` | SBERT 카테고리 분류 |
| `openai` | GPT-4o-mini LLM 추천 |
