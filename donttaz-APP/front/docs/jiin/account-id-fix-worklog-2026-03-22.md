# 계좌 ID store 미저장 버그 수정 워크로그 - 2026-03-22

## 1. 문제

홈 화면에서 "계좌 정보가 없습니다" 메시지가 표시되며 거래내역이 뜨지 않음.

---

## 2. 원인

### `useBankAccountStore`의 `accountId`가 null인 채로 유지됨

**계좌 개설 시 (`use-account-open-flow.ts`)**

```ts
// 수정 전
useBankAccountStore.getState().setAccount(
  currentAccount.accountId,  // ← null! 기존 store 값 그대로 사용
  ...
);
```

`createAccountApi` 응답(`CreateAccountResponse`)에 `accountId`가 포함되지 않음.
백엔드가 계좌 생성 응답으로 `bankCode`, `accountNo`, `currency`, `currencyName`만 반환하기 때문.
결과적으로 계좌 개설 완료 후에도 store의 `accountId`는 null.

**로그인 시 (`complete-login-session.ts`)**

```ts
// 수정 전 - mock이 아닌 경우
// 레거시 mock 계좌 정리만 하고 실제 계좌 fetch 없음
if (isLegacyMockAccount(...)) {
  currentAccount.clearAccount();
}
// accountId는 그대로 null
```

mock 모드가 아닌 경우 로그인 후 계좌 정보를 서버에서 가져오는 로직이 없었음.

---

## 3. 해결 방법

### `use-account-open-flow.ts` - `completeAccountCreation`

계좌 개설 후 `/accounts` API를 추가로 호출해서 실제 `accountId`를 가져옴.

```ts
const accountsResponse = await getMyAccountsApi();
const primaryAccount = accountsResponse.data.accountList.find((a) => a.isPrimary)
  ?? accountsResponse.data.accountList[0];

useBankAccountStore.getState().setAccount(
  primaryAccount?.accountId ?? null,
  createdBankName,
  accountsResponse.data.totalBalance,
  response.data.accountNo,
  accountOpenState.selectedProductName || createdAccountAlias
);
```

### `complete-login-session.ts`

로그인 시 store에 `accountId`가 없으면 `/accounts` API 호출해서 자동으로 채워줌.

```ts
if (!useBankAccountStore.getState().accountId) {
  const accountsResponse = await getMyAccountsApi();
  const account = accountsResponse.data.accountList.find((a) => a.isPrimary)
    ?? accountsResponse.data.accountList[0];
  if (account) {
    useBankAccountStore.getState().setAccount(
      account.accountId ?? null,
      account.bankName,
      accountsResponse.data.totalBalance,
      account.accountNo,
      account.accountName
    );
  }
}
```

---

## 4. 흐름 정리

```
계좌 개설 완료
  ↓
createAccountApi() → accountId 없음
  ↓
getMyAccountsApi() 추가 호출 → primaryAccount.accountId 획득
  ↓
useBankAccountStore.setAccount(accountId, ...) 저장
  ↓
홈 화면 거래내역 정상 표시
```

```
로그인
  ↓
completeLoginSession()
  ↓
store에 accountId 없음?
  → 없음: getMyAccountsApi() 호출 → store에 저장
  → 있음: 그대로 진행
  ↓
홈 화면 이동
```

---

## 5. 수정 파일

- `features/account-open/model/use-account-open-flow.ts`
- `features/auth/model/complete-login-session.ts`
