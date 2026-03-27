# Spending Report Monthly-First Plan - 2026-03-26

## 목적

리포트 기능은 우선 월간 화면을 완성하고, 연간은 탭과 진입 구조만 먼저 만들어 추후 API 연동이 쉬운 상태로 둔다.

이번 계획은 아래 원칙을 따른다.

- 연간 탭은 먼저 만든다.
- 연간 데이터 연동은 지금 하지 않는다.
- 월간 리포트는 백엔드 `GET /api/v1/ai/reports` 개선안을 기준으로 구현한다.
- 프론트 작업은 `고쳐야 할 것`과 `추가해야 할 것`으로 나눈다.

## 최종 방향

### 1. 화면 방향

- 리포트 화면은 `연간 탭 / 월간 탭` 구조로 간다.
- 기본 노출은 월간 탭으로 두는 것을 권장한다.
- 연간 탭은 준비 중 상태나 추후 제공 안내 화면으로 먼저 둔다.
- 실제 데이터 기반 구현은 월간 탭에 집중한다.

### 2. API 방향

- 월간 리포트는 공개 `GET /api/v1/ai/reports`를 수정해서 사용한다.
- 연간 리포트는 별도 API가 준비되기 전까지 프론트에서 실데이터 호출을 하지 않는다.

## 백엔드 수정 계획

## 1. 월간 리포트 API 계약 수정

대상:

- `GET /api/v1/ai/reports`

권장 응답 필드:

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
    "aiOverallFeedback": "종합 피드백",
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

설계 원칙:

- `categoryRate`는 도넛 차트용 소비 비중으로 사용한다.
- 예산 대비 달성률은 `budgetAchievementRate` 같은 별도 필드로 분리한다.
- `spendingPersona`, `feedback`, `personalizedFeedback`를 포함한다.

## 2. 집계 로직 수정

현재 문제:

- 플랜 카테고리만 순회하면 실제 지출이 누락될 수 있다.

수정 방향:

- `플랜 카테고리 + 실제 지출 카테고리` 합집합 기준으로 집계
- `totalSpent`는 실제 전체 지출 기준으로 계산
- 플랜에 없던 카테고리도 `categoryReports`에 포함
- `allocatedAmount = 0`이어도 실제 지출이 있으면 내려준다
- 정렬은 `spentAmount desc`로 고정한다

## 3. no-plan 정책 확정

정해야 할 것:

- 플랜이 없는 달을 계속 `400 FAIL`로 둘지
- AI 추정 예산 fallback으로 리포트를 생성할지

권장:

- 장기적으로는 fallback 지원
- 단기적으로는 에러를 유지하더라도 프론트가 구분 가능한 메시지와 코드 제공

## 4. 거래 없음 케이스 유지

거래가 없는 달은 아래 정책 유지가 좋다.

- `SUCCESS`
- `totalSpent = 0`
- `achievementRate = 0`
- `categoryReports`는 플랜 카테고리 기준 0원 목록
- `aiOverallFeedback` 포함

## 5. 연간 API는 후순위

이번 범위에서는 연간 실데이터 API를 필수로 넣지 않는다.

추후 필요 API 예시:

- `GET /api/v1/ai/reports/yearly?year=2026`

## 프론트에서 고쳐야 할 것

## 1. 월간 리포트 데이터 소스 교체

현재는 `transactions/categories` 기반 집계를 사용 중이다.

수정 대상:

- `entities/report/api/get-monthly-report.ts`
- `entities/report/model/use-monthly-report.ts`
- `entities/report/types/report.ts`

변경 방향:

- `GET /ai/reports` 응답을 직접 받는다
- 프론트에서 거래 목록을 다시 집계하지 않는다
- `categoryReports`, `aiOverallFeedback`, `personalizedFeedback` 중심으로 모델을 바꾼다

## 2. 월간 리포트 위젯 표시 로직 수정

수정 대상:

- `widgets/monthly-report/ui/monthly-report.tsx`
- `widgets/monthly-report/ui/monthly-report-donut-chart.tsx`
- `widgets/monthly-report/ui/monthly-report-legend.tsx`
- `widgets/monthly-report/ui/monthly-report-category-list.tsx`

변경 방향:

- 도넛 차트는 서버가 내려준 `categoryRate` 비중값을 사용
- 카테고리 리스트는 `allocatedAmount`, `spentAmount`, `feedback` 기준으로 렌더링
- 상단 요약은 `totalSpent`, `achievementRate`, `yearMonth` 기준으로 표시

## 3. 연간 기존 호출 제거 또는 비활성화

현재 연간 쪽은 없는 API를 보고 있으므로 정리 필요.

수정 대상:

- `entities/report/api/get-yearly-report.ts`
- `entities/report/model/use-yearly-report.ts`
- `entities/report/model/use-yearly-categories.ts`
- `widgets/yearly-report/ui/yearly-report.tsx`

변경 방향:

- 실제 API 호출 제거 또는 임시 비활성화
- 준비 중 상태 화면으로 대체
- 월간 탭 개발을 막지 않도록 분리

## 4. 라우트 구조 정리

권장 방향:

- 리포트 메인에서 탭 선택 가능
- 월간 탭을 기본값으로 둔다
- 연간 탭은 준비 중 화면

검토 대상:

- `pages/report/ui/report-page.tsx`
- `widgets/report-tab-selector/*`

## 프론트에서 추가해야 할 것

## 1. 월간 리포트 전용 타입

추가 권장 타입:

```ts
export interface SpendingReportCategoryItem {
  categoryName: string;
  allocatedAmount: number;
  spentAmount: number;
  categoryRate: number;
  budgetAchievementRate?: number;
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

## 2. 연간 준비중 UI

추가 내용:

- 연간 탭 placeholder
- 안내 문구
- 필요 시 아이콘 또는 간단한 설명 카드

예시 문구:

- `연간 소비 리포트는 곧 제공될 예정입니다.`
- `먼저 월간 리포트에서 이번 달 소비 흐름을 확인해보세요.`

## 3. 월간 요약 섹션

추가 권장 UI:

- 소비 페르소나 카드
- AI 종합 피드백 카드
- personalized feedback 리스트
- 예산 초과 여부 상태 배지

## 4. 상태 분기 UI

추가 필요:

- 로딩 상태
- 플랜 없음 상태
- 거래 없음 상태
- API 실패 상태

## 5. mock 데이터 재정리

추가 또는 수정 필요:

- `shared/mock/data`에 월간 리포트 API 응답 mock 추가
- 연간 탭용 placeholder 흐름 mock은 불필요
- 기존 `transactions/categories`, `transactions/yearly` 기반 mock 사용 여부 재검토

## 단계별 진행 순서

### Step 1. 백엔드 계약 확정

- 월간 `GET /ai/reports` 응답 필드 최종 확정
- `categoryRate` 의미를 소비 비중으로 확정
- 필요 시 `budgetAchievementRate` 추가

### Step 2. 백엔드 구현

- 집계 누락 수정
- 정렬 고정
- 추가 필드 반영
- 테스트 보강

### Step 3. 프론트 월간 리포트 전환

- 타입 교체
- API 교체
- 훅 교체
- 월간 위젯 반영

### Step 4. 프론트 리포트 메인 구조 정리

- 탭 구조 적용
- 연간 탭 placeholder 적용
- 기본 월간 탭 노출

### Step 5. mock 및 문서 정리

- mock 응답 갱신
- API 참고 문서 업데이트
- 구현 결정사항 문서화

## 이번 작업 범위에서 하지 않는 것

- 연간 실데이터 차트 구현
- 연간 API 미완성 상태에서 임시 합산 로직 억지 구현
- 공개 API와 다른 임시 프론트 계산으로 연간 차트 맞추기

## 권장 판단

이번 단계는 월간 완성도가 더 중요하다.

즉:

- 연간은 구조만 잡고
- 월간은 실제로 쓸 수 있게 완성하고
- 추후 연간 API가 생기면 같은 리포트 영역 안에서 자연스럽게 확장하는 것이 가장 안전하다
