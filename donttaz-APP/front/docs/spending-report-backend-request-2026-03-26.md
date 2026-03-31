# Spending Report Backend Request - 2026-03-26

## 목적

프론트는 리포트 기능을 `월간 우선`으로 구현하려고 합니다.

이번 요청의 목표는 아래와 같습니다.

- 월간 소비 리포트 화면을 실데이터 기반으로 먼저 완성
- 연간 리포트는 탭과 placeholder만 먼저 제공
- 추후 연간 API가 준비되면 자연스럽게 확장 가능하도록 계약 정리

## 현재 프론트 방향

- 리포트 메인 화면은 `연간 탭 / 월간 탭` 구조로 갑니다.
- 현재 단계에서는 `월간 탭`만 실제 데이터 연동을 진행합니다.
- `연간 탭`은 우선 준비 중 화면으로 처리합니다.

즉, 이번 요청의 핵심은 `GET /api/v1/ai/reports`를 월간 리포트 화면에 맞게 정리하는 것입니다.

## 현재 문제

프론트에서 확인한 현재 공개 API 기준 이슈는 아래와 같습니다.

### 1. `categoryRate` 의미가 화면 요구와 다릅니다

현재 공개 API의 `categoryReports[].categoryRate`는 예산 달성률에 가깝습니다.

하지만 월간 리포트 화면의 도넛 차트에는 아래 값이 필요합니다.

- `spentAmount / totalSpent * 100`
- 즉 카테고리 소비 비중

### 2. 플랜에 없는 실제 지출이 누락될 수 있습니다

현재는 플랜 카테고리만 기준으로 응답이 만들어질 가능성이 있어,
실제 소비가 리포트에서 빠질 수 있습니다.

월간 리포트는 실제 지출 누락 없이 보여주는 것이 중요합니다.

### 3. categoryReports 정렬 기준이 고정되어 있지 않습니다

프론트에서는 정렬이 일정해야 UI가 안정적으로 유지됩니다.

권장 정렬:

- `spentAmount desc`

### 4. 월간 화면에 필요한 부가 정보가 부족합니다

월간 리포트 화면에는 단순 금액 외에도 아래 정보가 있으면 좋습니다.

- 소비 페르소나
- 카테고리별 피드백
- 종합 피드백
- personalized feedback 리스트

## 이번 요청 범위

### 필수

- `GET /api/v1/ai/reports` 응답 구조 보강

### 후순위

- 연간 리포트 API 추가

## 우선 요청 사항

## 1. 월간 리포트 API 응답 구조 수정 요청

대상 API:

- `GET /api/v1/ai/reports?yearMonth=YYYY-MM`

프론트에서 필요한 응답 방향은 아래와 같습니다.

```json
{
  "status": "SUCCESS",
  "message": "소비 리포트 조회가 완료되었습니다.",
  "data": {
    "yearMonth": "2026-03",
    "sealedAmount": 300000,
    "totalBudget": 1500000,
    "totalSpent": 1943800,
    "achievementRate": 129.6,
    "spendingPersona": "쇼핑족",
    "aiOverallFeedback": "당신은 이번 달 쇼핑족 소비 패턴을 보였습니다.",
    "categoryReports": [
      {
        "categoryName": "기타",
        "allocatedAmount": 750000,
        "spentAmount": 900000,
        "categoryRate": 46.3,
        "budgetAchievementRate": 120.0,
        "feedback": "계획 대비 초과 소비입니다."
      }
    ],
    "personalizedFeedback": [
      {
        "type": "warning",
        "categoryName": "기타",
        "message": "기타가 계획 대비 150,000원 초과되었습니다."
      }
    ]
  }
}
```

## 2. 필드 정의 요청

### 공통 필드

- `yearMonth`
- `sealedAmount`
- `totalBudget`
- `totalSpent`
- `achievementRate`
- `spendingPersona`
- `aiOverallFeedback`
- `categoryReports`
- `personalizedFeedback`

### categoryReports 필드 정의

- `categoryName`: 카테고리명
- `allocatedAmount`: 플랜 기준 할당 예산
- `spentAmount`: 실제 지출액
- `categoryRate`: 월 전체 지출 대비 비중
- `budgetAchievementRate`: 해당 카테고리 예산 대비 달성률
- `feedback`: 카테고리별 피드백 문구

### personalizedFeedback 필드 정의

- `type`: `persona | warning | pattern | positive`
- `categoryName`: 카테고리 관련 없는 경우 `null`
- `message`: 표시 문구

## 3. categoryRate 의미 확정 요청

프론트 요청 기준:

- `categoryReports[].categoryRate = spentAmount / totalSpent * 100`

이 값은 월간 도넛 차트와 범례에 그대로 사용됩니다.

즉, `categoryRate`는 예산 달성률이 아니라 소비 비중으로 맞춰주시면 좋겠습니다.

예산 달성률은 아래처럼 별도 필드로 부탁드립니다.

- `budgetAchievementRate = spentAmount / allocatedAmount * 100`

## 4. 집계 기준 수정 요청

프론트 요청 기준:

- `플랜 카테고리 + 실제 지출 카테고리` 합집합 기준으로 `categoryReports` 생성
- 플랜에 없던 소비도 누락 없이 포함
- `allocatedAmount = 0`이어도 실제 지출이 있으면 응답에 포함
- `totalSpent`는 실제 전체 지출 합계 기준

예:

- 플랜에 없는 `금융/보험` 지출이 있으면 `categoryReports`에 포함

## 5. 정렬 기준 고정 요청

프론트 요청 기준:

- `categoryReports`는 `spentAmount desc` 정렬

이유:

- 도넛 차트, 범례, 카테고리 리스트 순서를 동일하게 유지하기 위함

## 6. 거래 없음 케이스 응답 유지 요청

거래가 없는 월에도 아래 형태면 프론트 처리에 좋습니다.

- `SUCCESS`
- `data` 존재
- `totalSpent = 0`
- `achievementRate = 0`
- `aiOverallFeedback` 포함
- 플랜 카테고리 기준으로 `spentAmount = 0`, `categoryRate = 0`

## 7. 플랜 없음 케이스 정책 확인 요청

현재 플랜이 없는 월은 실패 응답으로 이해하고 있습니다.

프론트에서는 아래 둘 중 하나로 정책이 확정되면 좋습니다.

### 옵션 A

- 계속 실패 응답 유지
- 대신 프론트가 구분 가능한 에러 코드 또는 명확한 메시지 제공

예:

- `NO_SPENDING_PLAN`
- `해당 월의 소비 플랜이 없습니다.`

### 옵션 B

- AI 추정 예산 기반 fallback 리포트 제공

현재 단계에서는 어떤 정책으로 갈지 확정만 부탁드립니다.

## 프론트 구현에 꼭 필요한 최소 조건

이번 월간 리포트 화면 구현을 위해 최소한 아래 4가지는 꼭 필요합니다.

1. `categoryRate`를 소비 비중으로 확정
2. 플랜 외 카테고리 소비 포함
3. `categoryReports` 정렬 기준 고정
4. `spendingPersona`, `feedback`, `personalizedFeedback` 포함 여부 확정

## 후순위 요청

이번 작업 범위 밖이지만 추후 연간 리포트를 위해 아래 API도 필요합니다.

- `GET /api/v1/ai/reports/yearly?year=2026`

최소 응답 예시:

```json
{
  "status": "SUCCESS",
  "message": "연간 소비 리포트 조회가 완료되었습니다.",
  "data": {
    "year": "2026",
    "monthlyTotals": [
      { "month": "01", "totalSpent": 546000 },
      { "month": "02", "totalSpent": 320000 },
      { "month": "03", "totalSpent": 548900 }
    ]
  }
}
```

이번 단계에서는 구현보다도 `추후 추가 예정 여부`만 공유받아도 충분합니다.

## 요청 정리

### 이번에 바로 부탁드리는 것

1. `GET /api/v1/ai/reports` 응답 계약 확정
2. `categoryRate`를 소비 비중으로 정리
3. 플랜 외 실제 지출 포함
4. `categoryReports` 정렬 기준 고정
5. `spendingPersona`, `feedback`, `personalizedFeedback` 응답 포함 여부 확정
6. no-plan 정책 확정

### 이번에 꼭 안 해도 되는 것

1. 연간 리포트 API 구현
2. 연간 통계 확장 응답

## 참고

프론트 내부 참고 문서:

- `front/docs/spending-report-api-reference-2026-03-26.md`
- `front/docs/spending-report-monthly-first-plan-2026-03-26.md`
