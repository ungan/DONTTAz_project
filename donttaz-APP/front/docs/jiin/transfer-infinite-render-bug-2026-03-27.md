# transfer 추가이체 Maximum Update Depth 버그

## 날짜
2026-03-27

## 증상
이체 완료 후 "추가 이체" 버튼을 누르면 `Maximum update depth exceeded` 에러 발생.

---

## 원인

### 1. 추가이체 시 같은 페이지가 두 개 떠 있다

"추가이체" 버튼은 `router.replace('/transfer-account')`로 이동한다.
`replace`는 **현재 화면(transfer-confirm)만** 새 화면으로 바꾸고, 그 아래 스택은 건드리지 않는다.

```
이체 전 스택:  [홈 → transfer-account① → transfer-amount → transfer-confirm]
추가이체 후:   [홈 → transfer-account① → transfer-amount → transfer-account②]
```

`transfer-account①`이 언마운트되지 않고 살아있기 때문에 `TransferAccountPage`가 **두 개** 동시에 마운트된 상태가 된다. 두 인스턴스가 같은 Zustand 스토어를 구독한다.

### 2. `accountOptions`가 렌더마다 새 배열로 만들어진다

`transfer-account-page.tsx`에서 `accountOptions`가 `useMemo` 없이 선언되어 있다.

```ts
const accountOptions = accounts.map(...); // 렌더할 때마다 새 배열
```

`useEffect`는 dependency가 바뀔 때 실행되는데, `accountOptions`가 매 렌더마다 새 배열(새 참조)이 되니까 **렌더마다 effect가 실행**된다.

```ts
useEffect(() => {
  if (hasSelectedAccount) return;
  setWithdrawalAccount(...); // 스토어 업데이트 → 재렌더
}, [accountOptions, withdrawalAccountNo, withdrawalAccountLabel]);
```

### 두 문제가 합쳐지면

1. ②가 `setWithdrawalAccount` 호출 → 스토어 변경
2. 스토어 변경 → ①② **둘 다** 재렌더
3. ①의 `accountOptions`가 새 참조 → ①의 effect 실행
4. ①이 또 `setWithdrawalAccount` 호출 → 스토어 변경
5. 2번으로 돌아가 반복
6. React 업데이트 한도(25회) 초과 → **에러**

---

## 수정

### 수정 1 — `pages/transfer-confirm/ui/transfer-confirm-page.tsx` (line 80)

스택 전체를 비우고 이동해서 두 번째 인스턴스 자체가 생기지 않게 한다.

```ts
// 기존
onPress={() => router.replace('/transfer-account')}

// 수정
onPress={() => {
  router.dismissAll();
  router.push('/transfer-account');
}}
```

### 수정 2 — `pages/transfer-account/ui/transfer-account-page.tsx`

`useMemo`는 dependency로 지정한 값이 **실제로 바뀔 때만** 재계산한다.
`accounts` 데이터가 같으면 같은 배열 참조를 재사용하므로, effect가 불필요하게 실행되지 않는다.

**① import에 `useMemo` 추가** (line 1)
```ts
import React, { useEffect, useMemo, useRef } from 'react';
```

**② `accountOptions` 선언부를 `useMemo`로 감싸기** (line 80–113)

기존 코드 내용은 그대로 두고 앞뒤만 감싼다.

```ts
const accountOptions = useMemo(() => {
  const options = accounts.map((account) => {
    // 기존 내용 그대로
  });

  if (options.length === 0 && storedAccountNo && ...) {
    options.push({ ... }); // 기존 내용 그대로
  }

  return options;
}, [accounts, storedAccountNo, storedAccountId, storedBankName, storedBalance, storedAccountName]);
```

---

## 수정 파일 요약

| 파일 | 수정 내용 |
|------|----------|
| `pages/transfer-confirm/ui/transfer-confirm-page.tsx` | 추가이체 버튼: `router.replace` → `router.dismissAll() + router.push` |
| `pages/transfer-account/ui/transfer-account-page.tsx` | `accountOptions` → `useMemo`로 감싸기 |
