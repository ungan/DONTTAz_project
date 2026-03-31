# Spending Report API Reference - 2026-03-26

## 목적

리포트 화면 구현 시 참고할 수 있도록, 2026-03-26 기준으로 확인한 AI 리포트 생성 요청/응답 예시와 공개 API 차이를 정리한다.

이 문서는 다음 상황에서 우선 참고한다.

- 월간 소비 리포트 화면 설계
- 카테고리 도넛 차트 데이터 모델 결정
- 공개 `GET /api/v1/ai/reports`와 AI 내부 리포트 생성 스펙 비교
- mock 데이터 또는 프론트 타입 설계

## 결론 요약

- 현재 공개 `GET /api/v1/ai/reports` 응답만으로는 도넛 차트용 비중 데이터가 부족하다.
- 아래 AI 리포트 생성 응답 예시는 `categoryReports.categoryRate` 합이 100이 되도록 내려와, 월간 소비 비중 UI와 잘 맞는다.
- 리포트 화면을 이 예시 기준으로 만들고 싶다면, 공개 API를 이 형태에 맞게 맞추거나 프론트에서 별도 어댑터를 둘 필요가 있다.
- 연간 화면은 별도 연간 API가 없으므로 이 문서만으로는 완성되지 않는다.

## AI 리포트 생성 요청 예시

```json
{
  "yearMonth": "2026-03",
  "paydayDay": 25,
  "plan": {
    "totalBudget": 1500000,
    "sealedAmount": 300000,
    "categoryPlans": [
      {
        "categoryName": "식비",
        "allocatedAmount": 400000,
        "aiComment": "배달 소비가 많아 식비 비중이 큽니다."
      },
      {
        "categoryName": "쇼핑",
        "allocatedAmount": 200000,
        "aiComment": "충동구매를 고려해 낮게 제한했습니다."
      },
      {
        "categoryName": "여가",
        "allocatedAmount": 150000,
        "aiComment": "야간 소비가 많아 주의가 필요합니다."
      },
      {
        "categoryName": "기타",
        "allocatedAmount": 750000,
        "aiComment": "예측 불가능한 지출 비중이 높습니다."
      }
    ]
  },
  "cardTransactions": [
    {
      "id": "card-01",
      "transactionDate": "2026-03-02",
      "amount": 18000,
      "counterpartyName": "배달의민족",
      "category": "식비",
      "memo": "야식",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-02",
      "transactionDate": "2026-03-02",
      "amount": 22000,
      "counterpartyName": "요기요",
      "category": "식비",
      "memo": "야식 2차",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-03",
      "transactionDate": "2026-03-05",
      "amount": 95000,
      "counterpartyName": "무신사",
      "category": "쇼핑",
      "memo": "충동 구매",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-04",
      "transactionDate": "2026-03-07",
      "amount": 7800,
      "counterpartyName": "스타벅스",
      "category": "여가",
      "memo": "카페",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-05",
      "transactionDate": "2026-03-10",
      "amount": 145000,
      "counterpartyName": "쿠팡",
      "category": "쇼핑",
      "memo": "대량 구매",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-06",
      "transactionDate": "2026-03-14",
      "amount": 32000,
      "counterpartyName": "배달의민족",
      "category": "식비",
      "memo": "야식",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-07",
      "transactionDate": "2026-03-18",
      "amount": 210000,
      "counterpartyName": "지그재그",
      "category": "쇼핑",
      "memo": "옷 구매",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-08",
      "transactionDate": "2026-03-20",
      "amount": 87000,
      "counterpartyName": "배달의민족",
      "category": "식비",
      "memo": "주말 폭식",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-09",
      "transactionDate": "2026-03-22",
      "amount": 132000,
      "counterpartyName": "오늘의집",
      "category": "쇼핑",
      "memo": "가구 구매",
      "type": "WITHDRAWAL"
    },
    {
      "id": "card-10",
      "transactionDate": "2026-03-23",
      "amount": 45000,
      "counterpartyName": "배달의민족",
      "category": "식비",
      "memo": "야식",
      "type": "WITHDRAWAL"
    }
  ],
  "accountTransactions": [
    {
      "id": "acc-01",
      "transactionDate": "2026-03-01",
      "amount": 2300000,
      "counterpartyName": "급여",
      "category": "income",
      "memo": "월급",
      "type": "DEPOSIT"
    },
    {
      "id": "acc-02",
      "transactionDate": "2026-03-03",
      "amount": 600000,
      "counterpartyName": "월세",
      "category": "기타",
      "memo": "고정지출",
      "type": "WITHDRAWAL"
    },
    {
      "id": "acc-03",
      "transactionDate": "2026-03-04",
      "amount": 120000,
      "counterpartyName": "통신비",
      "category": "기타",
      "memo": "고정지출",
      "type": "WITHDRAWAL"
    },
    {
      "id": "acc-04",
      "transactionDate": "2026-03-15",
      "amount": 250000,
      "counterpartyName": "적금",
      "category": "금융/보험",
      "memo": "저축",
      "type": "WITHDRAWAL"
    },
    {
      "id": "acc-05",
      "transactionDate": "2026-03-21",
      "amount": 180000,
      "counterpartyName": "카드값 결제",
      "category": "기타",
      "memo": "카드대금",
      "type": "WITHDRAWAL"
    }
  ]
}
```

## AI 리포트 생성 응답 예시

```json
{
  "status": "SUCCESS",
  "message": "분석 리포트 생성 완료",
  "data": {
    "yearMonth": "2026-03",
    "sealedAmount": 300000,
    "totalBudget": 1500000,
    "totalSpent": 1943800,
    "achievementRate": 129.6,
    "spendingPersona": "쇼핑족",
    "aiOverallFeedback": "당신은 이번 달 쇼핑족 소비 패턴을 보였습니다. 예산을 초과했으며, 특히 쇼핑과 기타 지출이 계획을 넘었습니다. 남은 기간 선택 지출을 최소화하는 것을 권장합니다.",
    "categoryReports": [
      {
        "categoryName": "기타",
        "allocatedAmount": 750000,
        "spentAmount": 900000,
        "categoryRate": 46.3,
        "feedback": "계획 대비 초과 소비입니다."
      },
      {
        "categoryName": "쇼핑",
        "allocatedAmount": 200000,
        "spentAmount": 582000,
        "categoryRate": 29.9,
        "feedback": "계획 대비 초과 소비입니다."
      },
      {
        "categoryName": "금융/보험",
        "allocatedAmount": 0,
        "spentAmount": 250000,
        "categoryRate": 12.9,
        "feedback": "계획이 없던 지출 항목입니다."
      },
      {
        "categoryName": "식비",
        "allocatedAmount": 400000,
        "spentAmount": 204000,
        "categoryRate": 10.5,
        "feedback": "계획 범위 내 소비입니다."
      },
      {
        "categoryName": "여가",
        "allocatedAmount": 150000,
        "spentAmount": 7800,
        "categoryRate": 0.4,
        "feedback": "계획 범위 내 소비입니다."
      }
    ],
    "personalizedFeedback": [
      {
        "type": "persona",
        "categoryName": null,
        "message": "당신은 이번 달 쇼핑족 소비 패턴을 보였습니다."
      },
      {
        "type": "warning",
        "categoryName": "기타",
        "message": "기타가 계획 대비 150,000원 초과되었습니다."
      },
      {
        "type": "warning",
        "categoryName": "쇼핑",
        "message": "쇼핑이 계획 대비 382,000원 초과되었습니다."
      },
      {
        "type": "pattern",
        "categoryName": null,
        "message": "1주차에 지출이 집중되는 패턴이 보입니다."
      },
      {
        "type": "positive",
        "categoryName": "여가",
        "message": "여가는 계획 범위 내에서 안정적으로 관리되었습니다."
      }
    ]
  }
}
```

## 화면 관점에서 좋은 점

### 1. 도넛 차트와 잘 맞는 카테고리 비중

예시 응답의 `categoryReports.categoryRate`는 합계가 100이다.

- 기타: 46.3
- 쇼핑: 29.9
- 금융/보험: 12.9
- 식비: 10.5
- 여가: 0.4

합계:

```txt
46.3 + 29.9 + 12.9 + 10.5 + 0.4 = 100.0
```

즉, 이 예시에서는 `categoryRate`가 카테고리 예산 달성률이 아니라 월 전체 지출 대비 비중으로 해석된다.

### 2. 플랜에 없던 지출도 표현 가능

`금융/보험`처럼 플랜에 없던 카테고리도 응답에 포함된다.

이 점은 실제 소비 누락 없이 월간 리포트를 보여주는 데 중요하다.

### 3. 카드형 요약 UI 확장에 유리

다음 필드들은 상세 리포트 화면 확장에 바로 활용 가능하다.

- `spendingPersona`
- `aiOverallFeedback`
- `personalizedFeedback`
- `categoryReports[].feedback`

## 공개 API와의 차이

현재 프론트 문서상 공개 API는 `GET /api/v1/ai/reports` 이다.

기존 확인 기준 차이는 아래와 같다.

### 1. `categoryRate` 의미 차이

- 공개 API 확인 결과:
  - `spentAmount / allocatedAmount * 100`
  - 즉 예산 달성률
- 이 문서의 AI 생성 응답 예시:
  - `spentAmount / totalSpent * 100`
  - 즉 카테고리 소비 비중

이 차이 때문에 공개 API 응답을 그대로 도넛 차트에 쓰면 의미가 어긋날 수 있다.

### 2. 카테고리 포함 범위 차이

- 공개 API는 플랜 카테고리 중심 응답일 가능성이 있다.
- 이 문서의 예시는 플랜에 없던 지출 카테고리까지 포함한다.

### 3. 정렬 차이

- 공개 API는 서버 정렬 보장이 약하다.
- 이 문서의 예시는 `spentAmount` 내림차순 형태로 해석하기 쉽다.

## 프론트 구현 권장안

### 월간 리포트

월간 리포트 화면은 가능하면 이 문서의 응답 형태를 기준으로 잡는 편이 좋다.

권장 화면 매핑:

- 상단 총 지출: `totalSpent`
- 예산 대비 상태: `achievementRate`
- 페르소나 문구: `spendingPersona`
- 종합 코멘트: `aiOverallFeedback`
- 도넛 차트: `categoryReports[].categoryRate`
- 카테고리 리스트: `categoryReports`
- 하단 상세 피드백 카드: `personalizedFeedback`

### 연간 리포트

이 문서만으로는 연간 리포트 데이터를 만들 수 없다.

추가로 최소 아래 중 하나가 필요하다.

- 월별 `totalSpent` 집계 API
- 연간 전체를 한 번에 내려주는 리포트 API

## 프론트 타입 설계 시 권장 형태

```ts
export interface SpendingReportCategoryItem {
  categoryName: string;
  allocatedAmount: number;
  spentAmount: number;
  categoryRate: number;
  feedback: string;
}

export interface SpendingReportFeedbackItem {
  type: 'persona' | 'warning' | 'pattern' | 'positive';
  categoryName: string | null;
  message: string;
}

export interface SpendingReportData {
  yearMonth: string;
  sealedAmount: number;
  totalBudget: number;
  totalSpent: number;
  achievementRate: number;
  spendingPersona: string | null;
  aiOverallFeedback: string;
  categoryReports: SpendingReportCategoryItem[];
  personalizedFeedback: SpendingReportFeedbackItem[];
}
```

## 권장 후속 작업

1. 공개 `GET /api/v1/ai/reports`를 이 문서의 응답 형태와 맞출지 결정
2. 아니면 프론트에서 공개 API -> 화면 모델 어댑터를 둘지 결정
3. 연간 리포트용 별도 API 스펙 정리
4. 이 응답 예시를 기준으로 mock 데이터와 타입 업데이트
