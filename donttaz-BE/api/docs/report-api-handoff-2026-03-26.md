# Spending Report API Handoff - 2026-03-26

## 목적

월간 소비 리포트 화면 연동을 위해 `GET /api/v1/ai/reports` 공개 API를 프론트 요구사항에 맞게 정리했다.

이번 작업의 핵심 목표:

- `categoryRate`를 소비 비중으로 제공
- `budgetAchievementRate`를 별도 제공
- 플랜 없는 월에도 fallback 리포트 제공
- `spendingPersona`, `feedback`, `personalizedFeedback` 포함
- `categoryReports` 정렬 기준 고정

---

## 이번에 반영한 내용

### 1. 공개 월간 리포트 응답 구조 확장

기존 `AiSpendingReportResponse`는 아래 수준의 단순 응답이었다.

- `yearMonth`
- `totalBudget`
- `totalSpent`
- `achievementRate`
- `aiOverallFeedback`
- `categoryReports[].categoryRate`

현재는 아래 필드까지 포함하도록 확장했다.

- `sealedAmount`
- `spendingPersona`
- `categoryReports[].budgetAchievementRate`
- `categoryReports[].feedback`
- `personalizedFeedback`

반영 파일:

- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/dto/response/AiSpendingReportResponse.java`

---

### 2. `GET /api/v1/ai/reports` 응답 메시지 고정

기존에는 글로벌 응답 래퍼 기본 메시지로 감싸졌는데, 월간 리포트 전용 메시지로 고정했다.

- `소비 리포트 조회가 완료되었습니다.`

반영 파일:

- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/controller/AiTransactionController.java`

---

### 3. Spring 공개 API 집계 방식 변경

기존 구현은 Spring `AiTransactionService#getSpendingReport(...)` 내부에서 직접 집계했다.

문제점:

- `categoryRate`가 예산 달성률에 가까웠음
- 플랜 카테고리 기준으로만 응답이 생성될 수 있었음
- `spendingPersona`, `category feedback`, `personalizedFeedback` 같은 richer 응답을 담기 어려웠음

현재는 AI 서버의 리포트 생성 엔드포인트를 호출하도록 변경했다.

- `POST /internal/ai/reports/generate`

Spring에서 하는 역할:

- 해당 월 거래 조회
- 거래를 card/account 트랜잭션 형태로 분리
- 플랜이 있으면 plan 포함
- 플랜이 없으면 `plan = null` 로 요청해서 fallback 리포트 유도
- AI 응답을 공개 API 응답 DTO로 매핑
- `categoryReports`를 `spentAmount desc`, 동률 시 `categoryName asc`로 정렬
- AI 응답에 `budgetAchievementRate`가 없으면 Spring에서 계산

반영 파일:

- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/service/AiTransactionService.java`
- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/service/AiServiceClient.java`

---

### 4. no-plan 정책 fallback 반영

기존:

- 플랜 없는 월은 `INVALID_REQUEST`

현재:

- 플랜이 없어도 AI 리포트 생성 요청을 보냄
- `plan = null`, `paydayDay` 포함
- AI가 fallback 예산/리포트를 생성하는 흐름으로 전환

추가 안전장치:

- AI 응답이 비어 있으면 Spring 로컬 fallback 집계 응답 생성

주의:

- 최종적으로 기대하는 richer fallback은 AI 서버가 만드는 것이고
- Spring 로컬 fallback은 비상용 안전망이다

---

### 5. AI 서버 호출용 DTO 추가

추가 파일:

- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/dto/request/AiSpendingReportGenerateRequest.java`
- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/dto/response/AiSpendingReportGenerateResponse.java`

설정 추가:

- `ai.report.endpoint=${AI_REPORT_ENDPOINT:/internal/ai/reports/generate}`

반영 파일:

- `donttaz-BE/api/src/main/resources/application.properties`

---

## 현재 공개 API 계약 요약

### 엔드포인트

- `GET /api/v1/ai/reports?yearMonth=YYYY-MM`

### 현재 응답 방향

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

---

## 필드 정의 정리

### 최상위 data

- `yearMonth`: 대상 연월
- `sealedAmount`: 봉인 금액
- `totalBudget`: 총 예산
- `totalSpent`: 총 실제 지출
- `achievementRate`: `totalSpent / totalBudget * 100`
- `spendingPersona`: 소비 페르소나
- `aiOverallFeedback`: AI 종합 피드백
- `categoryReports`: 카테고리 리포트 배열
- `personalizedFeedback`: 개인화 피드백 배열

### categoryReports

- `categoryName`: 카테고리명
- `allocatedAmount`: 플랜 기준 예산, 플랜에 없으면 `0`
- `spentAmount`: 실제 지출액
- `categoryRate`: 월 전체 지출 대비 비중
- `budgetAchievementRate`: 카테고리 예산 대비 달성률
- `feedback`: 카테고리별 피드백

### personalizedFeedback

- `type`: `persona | warning | pattern | positive` 중 하나를 기대
- `categoryName`: 카테고리와 무관하면 `null`
- `message`: 표시 문구

---

## 정렬 정책

`categoryReports` 정렬은 아래로 고정했다.

1. `spentAmount desc`
2. 동률 시 `categoryName asc`

---

## 테스트 정리

### 단위 테스트 가이드 적합성

`TEST_GUIDE.md` 기준 확인 결과:

- `@ExtendWith(MockitoExtension.class)` 사용
- `@Mock`, `@InjectMocks` 사용
- 서비스 단위 테스트 중심
- `Given - When - Then` 구조 유지
- `@SpringBootTest` 미사용

### 이번에 확인한 테스트

- `AiTransactionServiceTest`
- `AiTransactionReportServiceTest`

실행 명령:

```powershell
.\gradlew test --tests "com.donttaz.donttazapi.ai.service.AiTransactionServiceTest" --tests "com.donttaz.donttazapi.ai.service.AiTransactionReportServiceTest"
```

실행 결과:

- `BUILD SUCCESSFUL`

---

## 이번 작업 파일 목록

### 수정 파일

- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/controller/AiTransactionController.java`
- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/dto/response/AiSpendingReportResponse.java`
- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/service/AiServiceClient.java`
- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/service/AiTransactionService.java`
- `donttaz-BE/api/src/main/resources/application.properties`
- `donttaz-BE/api/src/test/java/com/donttaz/donttazapi/ai/service/AiTransactionServiceTest.java`

### 추가 파일

- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/dto/request/AiSpendingReportGenerateRequest.java`
- `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/dto/response/AiSpendingReportGenerateResponse.java`
- `donttaz-BE/api/src/test/java/com/donttaz/donttazapi/ai/service/AiTransactionReportServiceTest.java`

---

## 아직 남은 일

### 우선순위 높음

1. AI 서버 실제 응답이 현재 Spring DTO와 정확히 일치하는지 실서버 연동 확인
2. 공개 API 문서 업데이트
3. 프론트 월간 리포트와 실데이터 연동 확인

### 우선순위 중간

1. `AiTransactionServiceTest`의 월간 리포트 테스트를 더 최신 흐름 기준으로 보강
2. controller 레벨 응답 스냅샷 테스트 또는 API 문서 예시 업데이트
3. 로컬 fallback 응답에도 `personalizedFeedback`나 `spendingPersona`를 더 풍부하게 넣을지 검토

### 우선순위 낮음

1. 연간 리포트 API 설계
2. `GET /api/v1/ai/reports/yearly?year=2026` 추가
3. 프론트 연간 탭 placeholder 제거 후 실연동

---

## 바로 다음 추천 작업

가장 추천하는 다음 순서:

1. Swagger 또는 `ai_api.md`에 월간 리포트 응답 계약 반영
2. 프론트에서 `GET /api/v1/ai/reports` 실호출 테스트
3. AI 서버 실제 응답 예시 캡처
4. 연간 API는 별도 티켓으로 분리

---

## 메모

- no-plan 정책은 이제 실패가 아니라 fallback 방향이다
- 공개 API는 richer 응답을 내려주도록 바뀌었지만, AI 서버 쪽 계약이 바뀌면 Spring DTO도 같이 맞춰야 한다
- `TEST_GUIDE.md` 기준으로는 현재 테스트 작성 방식이 적합하다
