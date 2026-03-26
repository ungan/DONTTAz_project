# Spending Plan Integration Handoff - 2026-03-24

## 1. Current Status

- `POST /api/v1/ai/plans` 요청은 프론트에서 실제로 나가고 있음
- 현재 확인된 주요 이슈는 `Spring -> AI 서버 호출 실패` 또는 `AI 서버 endpoint 불일치` 가능성
- 프론트는 현재 `/ai/plans` 실패 시 fallback 데이터로 화면 확인 가능
- 따라서 지금은 `화면 확인`과 `실제 연동 점검`을 분리해서 보는 것이 좋음

## 2. Integration Checklist

### 2.1 Front -> Backend

- [ ] `POST /api/v1/ai/plans` 요청이 실제로 나가는지 확인
- [ ] `Authorization: Bearer ...` 토큰이 정상 포함되는지 확인
- [ ] request body에 `targetYearMonth`, `monthlyIncome`가 의도대로 들어가는지 확인
- [ ] 프론트에서 `/ai/plans` 성공 응답과 실패 fallback이 모두 정상 렌더링되는지 확인

### 2.2 Backend -> AI

- [ ] Spring 백엔드에서 `/api/v1/ai/plans` 진입 시 AI 호출 로그가 남는지 확인
- [ ] `AI_SERVER_URL` 실제 값 확인
- [ ] Spring이 호출하는 planning endpoint 확인
- [ ] AI 서버가 실제로 떠 있는지 확인
- [ ] AI 서버의 실제 planning endpoint가 무엇인지 확인
- [ ] Spring이 호출하는 endpoint와 AI 서버 실제 endpoint가 일치하는지 확인

### 2.3 Data Readiness

- [ ] 테스트 유저 1명 확보
- [ ] 해당 유저의 최근 3개월 거래내역 존재 여부 확인
- [ ] 거래내역 카테고리 분류 완료 여부 확인
- [ ] 월 수입(`monthlyIncome`) 테스트 값 확정
- [ ] 생성된 플랜이 DB에 저장되는지 확인
- [ ] 플랜 생성 후 대시보드(`/control/burndown`)까지 이어지는지 확인

### 2.4 Response Validation

- [ ] 응답 구조가 `status / message / data` 형태인지 확인
- [ ] `data.targetYearMonth` 존재 여부 확인
- [ ] `data.totalBudget` 존재 여부 확인
- [ ] `data.surplusAmount` 존재 여부 확인
- [ ] `data.categoryPlans`가 배열로 내려오는지 확인
- [ ] `categoryPlans` 안에 `categoryName`, `allocatedAmount`, `aiComment`가 들어오는지 확인
- [ ] 가능하면 `sealedAmount`, `riskScore`, `budgetStrategy`, `personalizedRecommendations`, `spendingPersona` 포함 여부도 확인

### 2.5 UI Validation

- [ ] 소비 플랜 상단 요약 카드 정상 표시
- [ ] 원형 차트 정상 표시
- [ ] 추천 문구 리스트 정상 표시
- [ ] 카테고리 상세 리스트 정상 표시
- [ ] `대시보드에서 실시간으로 확인하기` CTA 동작 확인
- [ ] 대시보드 차트/리스트 전환 동작 확인

## 3. Example Data Needed

현재 이 API는 request body에 거래내역을 직접 보내지 않고, 서버에 이미 적재된 사용자 데이터를 기준으로 플랜을 생성함.

즉 실제 연동 확인을 위해 필요한 최소 데이터는 아래와 같음.

- 테스트 유저 1명
- 최근 3개월 거래내역
- 거래별 카테고리 분류값
- 이번 달 예상 월수입

권장 샘플 조건:

- 최근 3개월 거래 20~30건 이상
- 카테고리 4개 이상 분산
- 식비, 교통, 생활, 쇼핑, 여가 중 최소 3개 이상 포함
- 한 카테고리에만 과도하게 몰리지 않게 구성
- `monthlyIncome` 예시값: `3000000`

권장 예시 분포:

- 식비: 월 45만~65만
- 교통: 월 10만~18만
- 생활: 월 20만~35만
- 쇼핑: 월 15만~30만
- 여가: 월 10만~25만
- 기타: 월 5만~15만

이 정도면 원형 차트, 카테고리 리스트, 추천 문구가 비교적 자연스럽게 보일 가능성이 높음.

## 4. Message For Backend Team

```text
안녕하세요. 소비 플랜 연동 확인 중입니다.

현재 프론트에서 `POST /api/v1/ai/plans` 요청은 실제로 나가고 있고, 백엔드까지는 도달하는 것으로 보입니다.
다만 이후 Spring에서 AI 서버 호출 단계에서 실패하는 정황이 확인되고 있어서 아래 내용 확인 부탁드립니다.

확인 요청 드리는 항목:
1. `/api/v1/ai/plans`의 현재 실제 응답 구조가 어떻게 되는지
2. 해당 API가 내부적으로 호출하는 AI 서버 URL과 planning endpoint가 무엇인지
3. 로컬/개발 환경에서 AI 서버 미기동 시 현재 의도된 동작이 무엇인지
4. 테스트용 유저 기준 최근 3개월 거래내역과 카테고리 분류 데이터가 준비되어 있는지
5. 플랜 생성 후 DB 저장까지 정상 동작하는지
6. 생성된 플랜을 기준으로 `/control/burndown`까지 이어지는지

프론트에서는 현재 화면 확인을 위해 `/ai/plans` 실패 시 fallback 데이터로 렌더링은 가능하게 해둔 상태입니다.
그래서 지금은 실제 API 응답 구조와 데이터 준비 여부를 먼저 맞추는 것이 중요해 보입니다.

가능하면 아래도 함께 부탁드립니다.
- `/api/v1/ai/plans` 성공 응답 샘플 1개
- 테스트 가능한 계정 1개
- 해당 계정의 데이터 준비 상태
```

## 5. Message For AI Team

```text
안녕하세요. 소비 플랜 AI 연동 확인 중입니다.

현재 Spring 백엔드에서 AI 서버 planning API를 호출하는 단계에서 연결 또는 endpoint 불일치 가능성을 확인하고 있습니다.
프론트 화면은 우선 확인 가능한 상태이고, 이제 실제 AI 응답으로 맞춰보려는 단계입니다.

확인 요청 드리는 항목:
1. 현재 AI 서버의 실제 base URL
2. 소비 플랜 생성용 실제 endpoint
3. 현재 로컬/개발 환경에서 AI 서버가 열려 있는지
4. planning API의 최신 request / response 스펙
5. 아래 필드들이 실제 응답에 포함되는지
   - `status`
   - `message`
   - `data.targetYearMonth`
   - `data.totalBudget`
   - `data.surplusAmount`
   - `data.categoryPlans[].categoryName`
   - `data.categoryPlans[].allocatedAmount`
   - `data.categoryPlans[].aiComment`
6. 가능하면 추가 포함 여부
   - `sealedAmount`
   - `riskScore`
   - `budgetStrategy`
   - `personalizedRecommendations`
   - `spendingPersona`

추가로 테스트를 위해 아래도 있으면 좋습니다.
- planning API 성공 응답 샘플 1개
- 최근 3개월 거래를 기준으로 정상 생성된 예시 응답
- endpoint 예시와 실행 방법

지금 프론트는 실제 연동 시도 중이고, 응답 필드가 정리되면 바로 화면 매핑 점검이 가능합니다.
```

## 6. Recommended Next Step

1. 백엔드 팀에 테스트 유저와 데이터 준비 상태 확인 요청
2. AI 팀에 실제 planning endpoint와 응답 샘플 요청
3. 응답 샘플 확보 후 프론트 매핑 최종 점검
4. 그다음 실제 AI 응답 기준으로 fallback 제거 여부 판단
