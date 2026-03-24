4. AI 기능
4.1 통합 거래내역 수집 요청 (Sync MyData Transactions)
4.1.1 개요
연동 시스템 : 모든 Client Device (또는 백엔드 스케줄러)
목적 : 마이데이터 API(또는 금융결제원 오픈뱅킹)를 호출하여 사용자의 타행 계좌에서 발생한 최신 거래 내역을 CONY 시스템 DB로 동기화(Sync)하여 긁어옵니다. 이후 AI 분석의 기초 데이터가 됩니다.
4.1.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/ai/transactions/sync
Method : POST


요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
로그인한 사용자의 토큰
Object
요청 바디
(Root)


Body
String
수집 시작일
startDate
2026-02-01
(Optional) 특정 기간만 수집 시 YYYY-MM-DD
String
수집 종료일
endDate
2026-03-05
(Optional) 미입력 시 마지막 수집일 이후부터 현재까지


4.1.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
최신 거래 내역 수집이 완료되었습니다.


Object
응답 데이터
data




Integer
신규 수집 건수
syncedCount
45
이번 요청으로 DB에 새로 추가된 거래내역 수
String
최종 수집 일시
lastSyncTime
2026-03-05T10:15:00
ISO 8601 포맷













4.2 카테고리 결과 조회 (Get AI Categorized Transactions)
4.2.1 개요
연동 시스템 : 모든 Client Device
목적 : 앞서 수집된(Sync) 사용자의 결제 내역을 바탕으로, AI(NLP) 모델이 1차적으로 추론하고 분류한 카테고리(식비, 교통비, 배달 등) 결과를 목록 형태로 조회합니다. 사용자가 맞춤 소비 플랜을 생성하기 전, 데이터가 올바르게 분류되었는지 확인하는 화면 렌더링용입니다.
4.2.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/ai/transactions/categories
Method : GET

요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
현재 로그인된 사용자의 Access Token
String
조회 대상 연월
yearMonth
2026-03
Query Parameter (YYYY-MM 형식)
Integer
페이지 번호
page
0
페이징 처리용 (기본값: 0)
Integer
페이지 크기
size
20
한 페이지당 데이터 개수 (기본값: 20)



4.2.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
카테고리 분류 결과 조회 성공


Object
응답 데이터
data




Integer
전체 내역 수
totalElements
85
해당 월의 전체 거래 건수
Array
분류 내역 리스트
categorizedList


최신순 정렬
Long
(List) 거래 ID
transactionId
5012
거래 내역 고유 식별자
String
(List) 결제 일시
transactionDate
2026-03-04T19:30:00


String
(List) 결제처
storeName
요기요_치킨
원본 결제처 이름
Integer
(List) 결제 금액
amount
23000


String
(List) 추론 카테고리
aiCategory
배달/외식
AI가 분석해 낸 1차 결과 값
Double
(List) 확신도(신뢰도)
confidenceScore
0.95
(Optional) AI가 이 분류를 얼마나 확신하는지 (0.0 ~ 1.0)
Boolean
(List) 사용자 수정 여부
isModified
false
사용자가 수동으로 카테고리를 바꿨는지 여부






















4.3 사용자가 카테고리 수정 (Update Transaction Category)
4.3.1 개요
연동 시스템 : 모든 Client Device
목적 : AI가 1차로 분류한 거래 내역의 카테고리가 부정확할 경우, 사용자가 직접 올바른 카테고리(예: 편의점 -> 식비)로 수정합니다. 이 수정 데이터는 향후 맞춤 소비 플랜 산출 및 AI 모델 재학습의 중요한 지표로 활용됩니다.
4.3.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/transactions/{transactionId}/category
Method : POST (또는 PATCH 권장)
요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
현재 로그인된 사용자의 Access Token
Long
경로 변수 (Path)
transactionId
5012
카테고리를 수정할 거래 내역 고유 ID
Object
요청 바디
(Root)


Body
String
변경할 카테고리
updateCategory
식비
사용자가 선택한 새로운 카테고리명 (또는 ID)




4.3.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
카테고리가 성공적으로 수정되었습니다.


Object
응답 데이터
data




Long
거래 ID
transactionId
5012


String
이전 카테고리
previousCategory
편의점
수정 전 AI가 분류했던 결과
String
수정된 카테고리
updatedCategory
식비


Boolean
사용자 수정 여부
isModified
true
상태가 true로 업데이트됨
String
수정 일시
modifiedAt
2026-03-05T10:45:00
ISO 8601 포맷






4.4 맞춤 소비 플랜 생성 (Create AI Spending Plan)
4.4.1 개요
연동 시스템 : 모든 Client Device
목적 : 수집 및 카테고리 분류가 완료된 최근 3개월치 거래 내역 데이터를 바탕으로, AI(NLP 및 군집화 분석) 모델을 구동하여 이번 달의 카테고리별 권장 예산과 '잉여 자금(금고 격리 대상)'을 산출합니다.
4.4.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/ai/plans
Method : POST
요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
로그인한 사용자의 토큰
Object
요청 바디
(Root)


Body
String
목표 연월
targetYearMonth
2026-03
플랜을 생성할 대상 월 (YYYY-MM)
Integer
예상 월 수입
monthlyIncome
3000000
(Optional) 급여 등 이번 달 예상 총 수입





4.4.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
맞춤 소비 플랜이 성공적으로 생성되었습니다.


Object
응답 데이터
data




Long
플랜 고유 ID
planId
305
생성된 예산 플랜의 ID
String
대상 연월
targetYearMonth
2026-03


Integer
산출된 총 예산
totalBudget
2500000
AI가 제안하는 이번 달 총 소비액
Integer
잉여 자금
surplusAmount
500000
수입에서 예산을 뺀 금액 (금고 이동 대상)
Array
카테고리별 예산
categoryPlans




String
(List) 카테고리명
categoryName
식비
배달/외식, 교통, 쇼핑 등
Integer
(List) 할당 예산
allocatedAmount
600000
해당 카테고리에 할당된 권장 금액
String
(List) AI 코멘트
aiComment
지난달 배달비 지출이 높았어요. 이번 달은 60만 원으로 줄여보는 건 어떨까요?
(Optional) 각 카테고리에 대한 AI 피드백

4.5 소비 리포트 조회 (Get AI Spending Report)
4.5.1 개요
연동 시스템 : 모든 Client Device
목적 : 특정 월의 AI 맞춤 소비 플랜 대비 실제 결제(지출) 내역을 비교 분석하여, 카테고리별 예산 달성률과 종합적인 소비 패턴 분석 리포트 데이터를 제공합니다.
4.5.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/ai/reports
Method : GET
요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
로그인한 사용자의 토큰
String
조회 대상 연월
yearMonth
2026-02
Query Parameter (YYYY-MM 형식). 미입력 시 이전 달 기준



4.5.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
소비 리포트 조회가 완료되었습니다.


Object
응답 데이터
data




String
대상 연월
yearMonth
2026-02


Integer
목표 총 예산
totalBudget
2500000
플랜에서 설정했던 총 예산
Integer
실제 총 지출액
totalSpent
2650000
해당 월에 실제로 쓴 총 금액
Integer
전체 예산 달성률
achievementRate
106
(totalSpent / totalBudget) * 100. (100 초과 시 예산 초과)
String
AI 종합 피드백
aiOverallFeedback
전체 예산을 15만 원 초과했어요. 특히 '배달/외식' 지출이 컸습니다.


Array
카테고리별 리포트
categoryReports




String
(List) 카테고리명
categoryName
배달/외식


Integer
(List) 할당 예산
allocatedAmount
600000


Integer
(List) 실제 지출액
spentAmount
850000


Integer
(List) 카테고리 달성률
categoryRate
141



















4.6 생존 번다운 차트 데이터 조회 (Survival Burndown Chart)
4.6.1 개요
연동 시스템 : 모든 Client Device
목적 : 사용자의 이번 달 총 예산을 기준으로, 일자별 이상적인 잔액(목표 선)과 실제 잔액(실제 선)을 비교하는 번다운 차트(Burndown Chart) 데이터를 제공합니다. 예산 소진 속도를 시각화하여 지출 통제를 유도합니다.
4.6.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/control/burndown
Method : GET
요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
로그인한 사용자의 토큰
String
조회 대상 연월
yearMonth
2026-03
Query Parameter (YYYY-MM). 미입력 시 이번 달








4.6.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
생존 번다운 차트 데이터 조회 성공


Object
응답 데이터
data




String
대상 연월
targetYearMonth
2026-03


Integer
총 할당 예산
totalBudget
2500000
차트의 시작점(Y축 최댓값)
Integer
현재 남은 예산
currentRemaining
1850000


Boolean
생존 여부
isSurviving
true
실제 선이 목표 선 아래로 곤두박질치지 않았는지 여부
Array
일자별 차트 데이터
chartData


1일부터 말일(또는 현재)까지의 배열
String
(List) 해당 일자
date
2026-03-05
YYYY-MM-DD
Integer
(List) 이상적 잔액
idealRemaining
2096774
(총 예산 / 해당 월의 일수)로 균등 차감된 목표 금액
Integer
(List) 실제 잔액
actualRemaining
1850000
해당 일자 자정 기준 실제 남은 예산
























4.7 금지 구역 설정 (Set Banned Categories)
4.7.1 개요
연동 시스템 : 모든 Client Device
목적 : 사용자가 지출을 강력하게 통제하고 싶은 특정 카테고리(예: 배달앱, 유흥비, 쇼핑 등)를 '금지 구역'으로 설정합니다. 향후 마이데이터 거래 내역 수집 시 해당 카테고리에서 결제가 발생한 것이 감지되면, 사전에 설정된 패널티(예: 유언장 웹훅 발송)가 자동으로 트리거됩니다.
4.7.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/control/banned-categories
Method : POST (또는 PUT)
요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
로그인한 사용자의 토큰
Object
요청 바디
(Root)


Body
Array
금지할 카테고리 목록
bannedCategories
["배달/외식", "유흥"]


String
적용할 패널티 타입
penaltyType
WEBHOOK
WEBHOOK(유언장 발송), LOCK(금고 동결) 등




4.7.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
금지 구역 설정이 완료되었습니다. 이제 해당 카테고리 결제 시 패널티가 부과됩니다.


Object
응답 데이터
data




Long
사용자 ID
userId
105


Array
설정된 금지 구역
activeBannedCategories
["배달/외식", "유흥"]
현재 활성화된 금지 카테고리 목록
String
패널티 타입
penaltyType
WEBHOOK


String
설정 일시
updatedAt
2026-03-05T11:00:00
ISO 8601 포맷



4.8 유언장 발송용 웹훅 URL 등록 (Register Penalty Webhook)(웹 훅 url 수정 필요함)
4.8.1 개요
연동 시스템 : 모든 Client Device
목적 : 금지 구역(Banned Category) 결제 등 패널티 조건이 충족되었을 때, 사용자의 지인들이 있는 외부 메신저 채널(Discord, Slack, 잔디 등)로 알림(유언장)을 자동 발송하기 위한 Webhook URL을 시스템에 등록합니다.
4.8.2 요청항목
Base URL : https://i14c104.p.ssafy.io/api/v1/control/webhooks
Method : POST












요청 Parameter
Type
Description
Element Name
Value
비고
Header
인증 토큰
Authorization
Bearer eyJhb...
로그인한 사용자의 토큰
Object
요청 바디
(Root)


Body
String
메신저 플랫폼
platform
DISCORD
DISCORD, SLACK, MATTERMOST 등
String
웹훅 URL
webhookUrl
https://discord.com/api/webhooks/...
발급받은 채널의 전체 Webhook 주소
String
유언장 메시지
messageTemplate
또 배달을 시켜먹었습니다. 절 욕해주세요.
(Optional) 전송될 커스텀 텍스트






4.8.3 응답항목
Type
Description
Element Name
Value
비고
String
응답 상태
status
SUCCESS


String
응답 메시지
message
유언장 발송용 웹훅 URL이 성공적으로 등록되었습니다.


Object
응답 데이터
data




Long
웹훅 고유 ID
webhookId
77


String
등록된 플랫폼
platform
DISCORD


String
마스킹된 URL
maskedUrl
https://discord.com/api/webhooks/***/...
보안상 일부 마스킹 처리
Boolean
활성화 여부
isActive
true



