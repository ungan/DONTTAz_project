# 수정 필요 사항 - 2026-03-22

## 현재 문제

계좌 생성(`POST /accounts`) 후 백엔드가 `accountId`를 null로 반환함.
DB에는 account가 정상 생성되어 있으나, sync 로직 내부에서 exception 발생 후 catch에서 null을 반환하는 구조.

```
finAPI 계좌 생성 → 성공
  ↓
syncCreatedAccount() → DB 저장 성공 (account_id=5)
  ↓
저장 이후 어딘가에서 RuntimeException 발생
  ↓
catch → null 반환
  ↓
CreateAccountResponse.accountId = null
```

---

## 백엔드 수정 필요 사항

**파일:** `AccountCreationService.java` - `syncCreatedAccount()` 메서드

**문제:** sync 중 DB 저장은 성공했는데 이후 exception이 발생하면 null 반환

**요청:** DB에 account가 정상 저장됐으면 accountId 반드시 반환하도록 수정

```java
// 현재
private Long syncCreatedAccount(...) {
    try {
        Account account = accountSyncService.syncCreatedAccount(...);
        return account.getAccountId();
    } catch (RuntimeException exception) {
        log.warn("...");
        return null;  // ← DB에 저장됐어도 null 반환
    }
}
```

**확인 방법:** 백엔드 콘솔에서 아래 로그 확인
```
account sync failed after external account creation accountNo=... message=...
```

---

## 프론트 임시 처리 (백엔드 fix 후 제거)

**파일 1:** `.env`
```
EXPO_PUBLIC_ACCOUNT_ID=5  # ← 삭제
```

**파일 2:** `features/auth/model/complete-login-session.ts`
```ts
// TODO: 백엔드 accountId 반환 fix 후 제거
if (!useBankAccountStore.getState().accountId) {
  const fallbackId = process.env.EXPO_PUBLIC_ACCOUNT_ID
    ? Number(process.env.EXPO_PUBLIC_ACCOUNT_ID)
    : null;
  if (fallbackId) {
    useBankAccountStore.getState().setAccount(fallbackId, 'DONTTAZ', 0, null, null);
  }
}
```
