# Spending Report Worklog - 2026-03-26

## 목적

리포트 기능 작업 현황을 정리하고, 다음 작업자가 바로 이어서 진행할 수 있도록 현재 상태와 남은 작업을 기록한다.

이번 작업의 기본 방향은 아래와 같았다.

- 리포트는 `월간 우선`으로 구현
- `연간 탭`은 구조만 먼저 만들고, 실제 데이터 연동은 보류
- 월간 리포트는 `GET /api/v1/ai/reports` 기준으로 프론트 구조를 먼저 맞춤

## 참고 문서

- `front/docs/spending-report-api-reference-2026-03-26.md`
- `front/docs/spending-report-monthly-first-plan-2026-03-26.md`
- `front/docs/spending-report-backend-request-2026-03-26.md`
- `donttaz-APP/FRONTEND_GUIDE.md`
- `front/docs/design-guide.md`

## 이번에 한 일

## 1. 리포트 메인 구조 변경

리포트 메인 화면을 `연간 / 월간` 탭 구조로 변경했다.

핵심 변경:

- 기본 탭을 `월간`으로 설정
- 연간은 준비중 placeholder로 처리
- 월간은 실제 리포트 위젯을 렌더링

관련 파일:

- `front/pages/report/ui/report-page.tsx`
- `front/pages/report/ui/report-page.styles.ts`
- `front/widgets/report-tab-selector/ui/report-tab-selector.tsx`
- `front/widgets/report-tab-selector/ui/report-tab-selector.styles.ts`
- `front/widgets/yearly-report/ui/yearly-report-placeholder.tsx`
- `front/widgets/yearly-report/ui/yearly-report-placeholder.styles.ts`

## 2. 월간 리포트 데이터 소스 변경

기존 `GET /ai/transactions/categories` 기반 집계 구조에서,
`GET /ai/reports` 응답을 직접 사용하는 구조로 바꿨다.

핵심 변경:

- 월간 타입 정의 교체
- 월간 API 경로 변경
- 카테고리/요약/피드백을 서버 응답 중심으로 사용

관련 파일:

- `front/entities/report/types/report.ts`
- `front/entities/report/api/get-monthly-report.ts`
- `front/entities/report/index.ts`

## 3. 월간 리포트 훅 재구성

`useMonthlyReport`에서 화면에 필요한 상태와 파생 데이터를 한 번에 계산하도록 정리했다.

추가된 처리:

- `summary`
- `categories`
- `topCategory`
- `budgetGap`
- `viewState`
- `statusMessage`

상태 분기:

- `ready`
- `empty`
- `no-plan`
- `error`

관련 파일:

- `front/entities/report/model/use-monthly-report.ts`
- `front/entities/report/model/report-category-colors.ts`

## 4. 월간 리포트 UI 확장

월간 리포트는 단순 차트 화면에서 요약형 리포트 화면으로 확장했다.

현재 표시되는 정보:

- 총 지출
- 총 예산
- 달성률
- 봉인 금액
- 소비 페르소나
- 예산 차이
- 최대 지출 카테고리
- AI 종합 피드백
- 카테고리별 소비 비중
- 카테고리별 예산/달성률/피드백
- personalized feedback

관련 파일:

- `front/widgets/monthly-report/ui/monthly-report.tsx`
- `front/widgets/monthly-report/ui/monthly-report.styles.ts`
- `front/widgets/monthly-report/ui/monthly-report-donut-chart.tsx`
- `front/widgets/monthly-report/ui/monthly-report-donut-chart.styles.ts`
- `front/widgets/monthly-report/ui/monthly-report-legend.tsx`
- `front/widgets/monthly-report/ui/monthly-report-legend.styles.ts`
- `front/widgets/monthly-report/ui/monthly-report-category-list.tsx`
- `front/widgets/monthly-report/ui/monthly-report-category-list.styles.ts`

## 5. 상태별 화면 처리 추가

월간 리포트에서 상태별 UI를 분리했다.

처리 상태:

- 로딩
- 플랜 없음
- 에러
- 지출 없음
- 정상 리포트

비고:

- `플랜 없음`은 향후 백엔드가 명확한 메시지 또는 코드로 내려주면 더 정확하게 맞출 수 있다.

## 6. mock 데이터 정리

mock 환경에서도 월간 리포트가 실제처럼 보이도록 응답 구조를 정리했다.

핵심 변경:

- `GET /ai/reports` mock 추가
- 기존 깨진 문자열 정리
- 실제 월간 리포트 구조에 맞게 categoryReports/personalizedFeedback 반영

관련 파일:

- `front/shared/mock/data/spending-report.json`
- `front/shared/mock/handlers.ts`

## 7. 연간 보조 코드 최소 정리

연간 리포트는 아직 placeholder 단계이지만,
리포트 타입 변경 때문에 깨지지 않도록 `useYearlyCategories`를 nullable 대응으로 정리했다.

관련 파일:

- `front/entities/report/model/use-yearly-categories.ts`

## 현재 상태 요약

현재 리포트 기능 상태는 아래와 같다.

### 완료된 것

- 월간 우선 구조 확정
- 리포트 메인 탭 구조 적용
- 연간 placeholder 적용
- 월간 리포트 타입/API/hook/UI 전환
- mock 응답 정리
- 상태 분기 UI 추가

### 아직 임시인 것

- 실제 백엔드 응답 미연동
- `no-plan` 판단은 메시지 기반 임시 처리
- 연간 리포트는 실데이터 없음

## 현재 기준 화면 동작

### `/report`

- 기본 진입 시 `월간 리포트` 탭 노출
- `연간 리포트` 탭 선택 시 준비중 카드 노출

### 월간 리포트

- mock 환경에서는 `GET /ai/reports` 기준 데이터로 렌더링
- 도넛/범례/카테고리 리스트/맞춤 피드백까지 표시
- 데이터가 없거나 실패하면 상태 카드 노출

## 검증 결과

`npm.cmd run check-types` 기준,
리포트 작업과 무관한 기존 타입 오류 2건이 남아 있다.

남은 오류:

- `front/entities/plan/api/create-spending-plan.ts`
- `front/pages/vault-webhook/ui/vault-webhook-page.tsx`

즉, 리포트 변경으로 새로 생긴 타입 오류는 현재 기준 없다.

## 앞으로 해야 할 일

## 1. 백엔드 실제 응답 붙이기

우선순위 가장 높음.

필요 작업:

- 실제 `GET /api/v1/ai/reports` 응답 구조 확인
- `categoryRate` 의미 최종 확인
- `budgetAchievementRate` 포함 여부 확인
- `personalizedFeedback` 실제 포함 여부 확인
- `spendingPersona` 실제 포함 여부 확인

프론트 수정 포인트:

- `front/entities/report/types/report.ts`
- `front/entities/report/model/use-monthly-report.ts`
- `front/shared/mock/data/spending-report.json`

## 2. no-plan 정책 최종 반영

현재는 메시지 기반 임시 처리다.

백엔드가 아래 중 하나를 확정하면 맞춰 수정해야 한다.

- 명확한 에러 코드 제공
- 명확한 메시지 제공
- fallback 리포트 제공

## 3. 월간 리포트 마감

실서버 응답이 붙으면 아래를 다듬으면 된다.

- 상태 카드 카피 보정
- 피드백 중요도 시각 차이 조정
- 카테고리 피드백 문구 길이 대응
- 숫자 포맷 최종 통일

## 4. 연간 리포트 실제 구현

현재는 placeholder 단계.

추후 필요:

- 연간 API 확정
- `useYearlyReport` 재설계
- 연간 차트와 월 선택 흐름 연결

## 5. 문서 업데이트

백엔드 응답이 확정되면 아래 문서를 함께 업데이트해야 한다.

- `front/docs/spending-report-api-reference-2026-03-26.md`
- `front/docs/spending-report-monthly-first-plan-2026-03-26.md`
- 필요 시 `front/docs/spending-report-backend-request-2026-03-26.md`

## 다음 작업 추천 순서

1. 백엔드 실제 `GET /ai/reports` 응답 샘플 확보
2. 프론트 타입/어댑터 최종 맞춤
3. mock 제거 또는 실응답 기준 갱신
4. 월간 UI 마감
5. 연간 API 준비 후 연간 리포트 구현

## 인계 메모

- 현재 구조는 `월간 우선` 전략에 맞춰 잘 정리돼 있음
- 연간은 아직 placeholder이므로 API가 없더라도 리포트 메뉴 자체는 정상 동작함
- 월간 리포트는 mock 기준으로는 바로 확인 가능함
- 다음 작업자는 백엔드 응답 확정 이후 `entities/report` 계층만 먼저 손보면 대부분 이어갈 수 있음
