# 프로젝트 이후 개선: 번다운 차트 일별 합계와 입금 판정 (김회인)

> 이 브랜치(`refactor/번다운-일별-합계-DB-집계`)의 변경은 **프로젝트가 끝난 뒤 개인적으로 한 작업**입니다.
> 팀 최종본은 `master`이고, 이 브랜치는 그 위에 본인이 작성한 코드의 개선만 올렸습니다.

## 1. 번다운 차트: 거래 전량 적재 → DB 날짜별 집계

**문제**
`AiTransactionService.getBurndownChart`가 한 달 거래를 전부 엔티티로 불러온 뒤 메모리에서 일별 합계를 냈습니다.

**처음 시도와 재측정**
500건씩 페이징해 한 번에 올리는 행 수를 묶어 봤지만, 워밍업과 실행 순서를 통제해 다시 재 보니 오히려 느렸습니다.
페이지마다 OFFSET 조회와 count 쿼리가 따로 나가기 때문입니다(거래 3만 건이면 SQL 120문).

**해결**
필요한 값은 일별 합계뿐이므로 DB에서 날짜별로 집계합니다.
`TransactionRepository.sumSpendingByDay`: `cast(transaction_at as date)`로 `GROUP BY`, 기존 `(user_id, transaction_at)` 인덱스 범위를 그대로 씁니다.

| 거래 수 | 전량 적재 (기존) | 500건 페이징 (시도) | DB 집계 (최종) | 받는 행 |
|---|---|---|---|---|
| 3,000 | 22.2ms | 67.9ms | **0.9ms** | 3,000 → 21 |
| 30,000 | 124.1ms | 2,455.3ms | **0.7ms** | 30,000 → 209 |

- 측정: H2 · `@DataJpaTest` · 세 방식을 3회 워밍업한 뒤 라운드마다 실행 순서를 돌려 5회씩 측정한 중앙값 (`performance/burndown-daily-sum-2026-11-26.txt`)
- 받는 행 수는 날짜 수입니다. 실제 번다운은 한 달 범위라 최대 31행입니다
- 한계: H2는 같은 프로세스 안의 DB라 네트워크 전송 비용이 없습니다. MySQL에서 잰 값이 아닙니다
- 재현: `./gradlew test --tests '*AiBatchBenchmarkRepeatTest'` (규모 변경은 JVM 옵션 `-Dbench.tx=30000`)

## 2. 입금 판정 통일

입금은 저장 경로마다 `tx_type` 값이 다릅니다. 거래 동기화(`TransactionSyncService`)는 `'DEPOSIT'`으로 정규화하고, 이체(`AccountTransferService`)는 은행 원본 코드 `'1'`(출금은 `'2'`)로 저장합니다.

- 번다운은 거래 유형을 가리지 않아 입금도 지출로 더했습니다 → 집계 쿼리에서 `'DEPOSIT'`과 `'1'`을 모두 뺍니다
- AI 실패 대응 리포트와 AI 요청의 계좌/카드 분류는 `'DEPOSIT'`만 입금으로 봤습니다 → `AiTransactionService`의 입금 판정 기준 하나(`DEPOSIT_TX_TYPES`)로 통일하고, AI 서버로 보내는 유형도 `DEPOSIT`/`WITHDRAWAL`로 맞춥니다

## 3. 검증

- `TransactionRepositoryDailySumTest`: 자정 직전·직후, 월 시작·끝 경계, 다른 사용자, 입금 코드(`'DEPOSIT'`, `'1'`)를 섞은 데이터로 기존 방식과 결과 비교. 월급 300만 원이 들어온 날의 지출이 기존 방식 3,005,000원 → 수정 후 5,000원임을 확인
- `AiTransactionReportServiceTest`: 이체 입금이 계좌 쪽으로 분류되는지, AI 실패 시 fallback 리포트가 모든 입금 코드를 빼는지
- `AiBatchBenchmarkRepeatTest`: 세 방식의 날짜별 합계가 매 회차 같은지 검사하며 측정
- 기존 단위 테스트(`AiTransactionServiceTest`)는 새 쿼리 기준으로 수정
