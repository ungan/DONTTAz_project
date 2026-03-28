# 송금 내역 홈화면 미표시 버그

작성일: 2026-03-28

---

## 증상

홈화면 결제내역 위젯에서 최근 송금(이체) 트랜잭션이 표시되지 않음.
금고 입금/출금 내역은 정상 표시. DB에는 해당 레코드 존재 확인.

---

## FE 디버깅 결과

`usePayments`에 콘솔 로그 추가 후 확인:

```
totalElements: 45   ← DB에는 45건 있음
transactions count: 3

반환된 3건:
- 177453  금고 입금            2026-03-28T10:45:54
- 177389  금고 중도 해지 환급  2026-03-27T22:07:49
- 177385  금고 출금 지급       2026-03-27T22:02:52
```

**기대값:**
- 177453  2026-03-28T10:45:54
- **177390  송금(이체)  2026-03-27T??:??:??**  ← 누락
- 177389  2026-03-27T22:07:49

→ FE는 API 응답을 그대로 표시. **FE 로직 문제 없음.**

---

## 원인 분석 (BE 코드 직접 확인)

### 문제 파일
`AccountTransferService.java` — `toTransactionAt()` 메서드

```java
private LocalDateTime toTransactionAt(String transactionDate) {
    try {
        return LocalDate.parse(transactionDate, DateTimeFormatter.BASIC_ISO_DATE)
                        .atStartOfDay();  // ← 원인
    } catch (DateTimeParseException exception) {
        return LocalDateTime.now();
    }
}
```

`BASIC_ISO_DATE`는 `yyyyMMdd` 날짜만 파싱하고 `.atStartOfDay()`로 시간을 **자정(00:00:00)으로 고정**.
→ 송금 트랜잭션 177390의 `transactionAt`이 `2026-03-27T00:00:00`으로 저장됨.

### 정렬 순서가 밀리는 이유

거래내역 조회 쿼리: `ORDER BY transactionAt DESC` + `size=3`

| transactionId | transactionAt | 정렬 순위 |
|---|---|---|
| 177453 | 2026-03-28T10:45:54 | 1 |
| 177389 | 2026-03-27T22:07:49 | 2 |
| 177385 | 2026-03-27T22:02:52 | 3 |
| **177390** | **2026-03-27T00:00:00** | **4 ← 잘림** |

같은 날 실제 시각이 있는 트랜잭션들이 앞에 오고, 자정으로 저장된 송금 내역이 밀려 첫 페이지에서 누락.

### 왜 금고 트랜잭션은 정상인가

`TransactionSyncService.resolveTransactionAt()`은 은행 API의 실제 `LocalDateTime`을 그대로 저장.
`AccountTransferService.toTransactionAt()`만 이 버그를 가지고 있음.

---

## BE 수정 요청

**파일:** `AccountTransferService.java`
**메서드:** `toTransactionAt()`

```java
// 현재 (버그)
.atStartOfDay()

// 수정 — 실제 처리 시각 사용
LocalDate.parse(transactionDate, DateTimeFormatter.BASIC_ISO_DATE)
         .atTime(LocalDateTime.now().toLocalTime());
// 또는
return LocalDateTime.now();
```

---

## 영향 범위

- 홈화면 결제내역 (size=3) — 체감 가장 큼
- 전체 결제내역 페이지는 스크롤하면 나타나므로 상대적으로 덜함
- 동일 날짜에 트랜잭션이 많을수록 송금 내역이 더 뒤로 밀림

## FE 대응

BE 수정 전까지 FE에서 취할 수 있는 조치 없음.
