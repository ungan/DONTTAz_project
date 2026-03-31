# 돈땃쥐 프론트엔드 개발 가이드

> React Native / Expo를 처음 접하는 팀원을 위한 가이드

---

## 목차

1. [환경 설정 (처음 1번만)](#1-환경-설정-처음-1번만)
2. [프로젝트 실행하기](#2-프로젝트-실행하기)
3. [Expo로 결과 확인하는 방법](#3-expo로-결과-확인하는-방법)
4. [React Native 기본 개념](#4-react-native-기본-개념)
5. [프로젝트 구조 이해하기 (FSD 아키텍처)](#5-프로젝트-구조-이해하기-fsd-아키텍처)
6. [코드 작성 규칙 — 어디에 무엇을 넣는가](#6-코드-작성-규칙--어디에-무엇을-넣는가)
7. [API 작업 가이드 — entities vs features](#7-api-작업-가이드--entities-vs-features)
8. [새 화면·기능 추가 실전 순서](#8-새-화면기능-추가-실전-순서)
9. [Mock 데이터 사용법](#9-mock-데이터-사용법)
10. [자주 쓰는 명령어 모음](#10-자주-쓰는-명령어-모음)
11. [트러블슈팅](#11-트러블슈팅)

---

## 1. 환경 설정 (처음 1번만)

### 1-1. Node.js 설치

이 프로젝트는 **Node.js v24.12.0** 를 사용합니다.
버전이 다르면 패키지 설치나 실행에서 오류가 날 수 있으니 반드시 맞춰야 합니다.

#### 직접 설치

- Node.js 공식 사이트(nodejs.org)에서 **v24.12.0 LTS** 다운로드 후 설치.
- 설치 가이드: https://married-spot-253.notion.site/Node-js-1925d5119f4f4cf3b5c17002025c05d7

---

### 1-2. Expo CLI 설치

Expo는 React Native 앱을 빌드하고 실행하는 도구입니다.

```bash
npm install -g expo-cli
```

---

### 1-3. Expo Go 앱 설치 (스마트폰에서 테스트)

실제 폰에서 앱을 바로 확인하려면 **Expo Go** 앱이 필요합니다.

- **Android**: Google Play Store → "Expo Go" 검색 후 설치
- **iOS**: App Store → "Expo Go" 검색 후 설치

> 폰과 개발 PC가 **같은 와이파이**에 연결되어 있어야 합니다.

---

### 1-4. 프로젝트 의존성 설치

```bash
# 프로젝트 폴더로 이동
cd front

# 패키지 설치 (node_modules 폴더가 생성됨)
npm install
```

설치가 완료되면 `node_modules/` 폴더가 생깁니다. 이 폴더는 Git에 올리지 않습니다.

---

### 1-5. 환경변수 파일 확인

`front/.env` 파일이 있는지 확인하세요. 없으면 아래 내용으로 만드세요: (저에게 물어 보세요)


## 2. 프로젝트 실행하기

```bash
# front/ 폴더 안에서 실행
cd front
npm start
```

실행하면 터미널에 QR코드와 함께 아래와 같은 화면이 나옵니다:<br>
QR을 스캔하면 지금까지 작업한 내용을 expo GO 앱에서 확인할 수 있습니다.

```
Metro waiting on exp://192.168.x.x:8081
› Scan the QR code above with Expo Go (Android) or the Camera app (iOS)
› Press a │ open Android
› Press i │ open iOS simulator
› Press w │ open web
```

---

## 3. Expo로 결과 확인하는 방법

### 스마트폰 (가장 빠름)

1. `npm start` 실행
2. 터미널에 QR코드가 나옵니다
3. **Android**: Expo Go 앱 열기 → "Scan QR Code" → QR 스캔
4. **iOS**: 기본 카메라 앱으로 QR 스캔 → 링크 탭

> 코드를 수정하고 저장하면 **자동으로 앱이 새로고침** 됩니다 (Hot Reload).


### 파일 수정 후 확인 흐름

```
코드 수정 → 파일 저장 (Ctrl+S) → 앱 자동 새로고침 → 확인
```

> 자동 새로고침이 안 될 때: 앱에서 두 손가락으로 흔들기 → "Reload" 선택

---

## 4. React Native 기본 개념

FSD 구조를 보기 전에, 프로젝트에서 반복적으로 등장하는 개념들을 먼저 짚습니다.

### UI — View, Text, StyleSheet

React Native는 HTML 태그 대신 RN 전용 컴포넌트를 씁니다.

```tsx
import { View, Text, StyleSheet } from 'react-native';

export function MyComponent() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>안녕하세요</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 16, backgroundColor: '#fff' },
  title: { fontSize: 20, fontWeight: 'bold' },
});
```

- `View` = `<div>`, `Text` = `<p>/<span>` 에 해당
- 스타일은 CSS가 아닌 `StyleSheet.create()`로 작성 (camelCase)
- 스타일 파일은 같은 폴더에 `*.styles.ts`로 분리하는 게 이 프로젝트 규칙

---

### types — TypeScript 타입 정의

서버 응답, 함수 인자 등 모든 데이터 형태를 미리 정의합니다.

```typescript
// types/payment.ts
export interface Transaction {
  transactionId: number;
  amount: number;
  counterpartyName: string;
  type: 'DEPOSIT' | 'WITHDRAWAL';
}

export interface GetPaymentsResponse {
  status: string;
  data: { transactions: Transaction[] };
}
```

- 같은 폴더의 `types/` 디렉터리에 모아둡니다
- `interface`는 객체 모양 정의, `type`은 유니온(`'A' | 'B'`) 등에 활용

---

### Store — Zustand 전역 상태

여러 화면에서 공유해야 하는 데이터(로그인 유저, 계좌 정보 등)는 **Zustand store**에 담습니다.
`useState`는 해당 컴포넌트 안에서만 살지만, store는 앱 어디서든 꺼내 쓸 수 있습니다.

```typescript
// entities/session/model/session-store.ts
import { create } from 'zustand';

export const useSessionStore = create<{
  name: string | null;
  setSession: (name: string) => void;
}>((set) => ({
  name: null,
  setSession: (name) => set({ name }),
}));
```

```tsx
// 컴포넌트에서 사용
const { name } = useSessionStore();       // 읽기
const { setSession } = useSessionStore(); // 쓰기
```

- `persist` 옵션을 붙이면 앱을 껐다 켜도 데이터가 유지됩니다 (AsyncStorage 사용)

---

### Hook — 로직 분리

컴포넌트에서 비즈니스 로직을 분리할 때 `use`로 시작하는 커스텀 훅을 씁니다.
컴포넌트는 훅만 호출하고 UI에만 집중합니다.

```typescript
// model/use-payments.ts
export function usePayments() {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isPending, setIsPending] = useState(false);

  useEffect(() => {
    setIsPending(true);
    getPaymentsApi()
      .then((res) => setTransactions(res.data.transactions))
      .finally(() => setIsPending(false));
  }, []);

  return { transactions, isPending };
}
```

```tsx
// 컴포넌트는 훅만 호출
export function PaymentHistory() {
  const { transactions, isPending } = usePayments();
  if (isPending) return <ActivityIndicator />;
  return <FlatList data={transactions} ... />;
}
```

---

### index.ts — 공개 API

각 폴더는 `index.ts` 하나로 외부에 공개할 것을 선언합니다.
내부 파일 구조가 바뀌어도 import 경로는 그대로 유지됩니다.

```typescript
// entities/payment/index.ts
export { getPaymentsApi } from './api/get-payments';
export { usePayments } from './model/use-payments';
export type { Transaction } from './types/payment';
```

---

## 5. 프로젝트 구조 이해하기 (FSD 아키텍처)

이 프로젝트는 **FSD (Feature-Sliced Design)** 아키텍처를 사용합니다.
"어떤 코드를 어디에 두어야 하나"를 명확하게 정의한 규칙입니다.

### 폴더 구조 한눈에 보기

```
front/
├── app/              ← 라우팅 (Expo Router 파일 기반)
├── pages/            ← 화면 단위 조합 컴포넌트
├── widgets/          ← 여러 화면에서 재사용되는 큰 컴포넌트 (UI 모음)
├── features/         ← 사용자 행동 단위 (로그인, 결제 등)
├── entities/         ← 도메인 데이터 (계좌, 유저, 결제내역 등)
└── shared/           ← 공통 유틸, UI, API 클라이언트
```

### 레이어 설명

```
┌─────────────────────────────────┐
│  app/        라우팅, 레이아웃    │  ← 최상위
├─────────────────────────────────┤
│  pages/      화면 조합           │
├─────────────────────────────────┤
│  widgets/    재사용 큰 덩어리    │
├─────────────────────────────────┤
│  features/   사용자 행동/기능    │
├─────────────────────────────────┤
│  entities/   도메인 모델/데이터  │
├─────────────────────────────────┤
│  shared/     공통 도구           │  ← 최하위
└─────────────────────────────────┘
```

**중요 규칙**: 위 레이어는 아래 레이어만 import할 수 있습니다.
예) `features/` → `entities/` import 가능 / `entities/` → `features/` import 불가

### 각 폴더 세부 설명

#### `app/` — 라우팅

Expo Router를 사용한 **파일 기반 라우팅**입니다. 파일을 만들면 자동으로 화면이 됩니다.

```
app/
├── _layout.tsx         ← 앱 전체 레이아웃 (폰트 로딩, 테마)
├── index.tsx           ← 첫 화면 (스플래시)
├── login.tsx           ← /login 경로
└── (tabs)/
    ├── _layout.tsx     ← 탭 네비게이션 레이아웃
    ├── home.tsx        ← /home 경로
    ├── menu.tsx        ← /menu 경로
    ├── plan.tsx        ← /plan 경로
    └── vault.tsx       ← /vault 경로
```

새 탭을 추가하려면 `app/(tabs)/` 안에 파일을 만들면 됩니다.

#### `pages/` — 화면

각 화면의 레이아웃을 조합하는 곳. `widgets/`와 `features/`의 UI를 가져다 배치합니다.

```
pages/home/
├── ui/
│   ├── home-page.tsx        ← 실제 홈화면 컴포넌트
│   └── home-page.styles.ts  ← 스타일 (StyleSheet)
└── index.ts                 ← 외부에 공개할 것만 export
```

#### `widgets/` — 재사용 큰 컴포넌트

여러 페이지에서 공통으로 쓰이는 UI 덩어리. 독립적으로 동작합니다.

```
widgets/payment-history/    ← 결제내역 위젯
widgets/nav-bar/            ← 하단 네비게이션 바
widgets/welcoming-alert/    ← 상단 환영 메시지
widgets/hero-banner/        ← 마스코트 배너
widgets/favorite-menu/      ← 즐겨찾기 메뉴
```

#### `features/` — 기능

사용자의 **행동** 단위로 묶습니다. 로그인, 로그아웃, 결제 요청 같은 것들.

```
features/auth/
├── api/
│   └── login.ts         ← API 호출 함수
├── model/
│   ├── use-login.ts     ← 로그인 로직 훅
│   └── use-auth-check.ts
├── types/
│   └── login.ts         ← 타입 정의
├── ui/
│   └── login-form-section.tsx  ← 로그인 폼 UI
└── index.ts
```

#### `entities/` — 도메인 엔티티

앱의 **핵심 데이터 모델**. 서버에서 받아오거나 앱 전체에서 공유하는 데이터.

```
entities/payment/
├── api/
│   └── get-payments.ts  ← 결제내역 조회 API
├── model/
│   └── use-payments.ts  ← 결제내역 훅 (상태 관리)
├── types/
│   └── payment.ts       ← Transaction 타입 정의
└── index.ts
```

#### `shared/` — 공통

어디서든 사용할 수 있는 가장 기본적인 것들.

```
shared/
├── api/
│   └── axios-instance.ts  ← HTTP 클라이언트 설정
├── mock/
│   ├── handlers.ts        ← 가짜 API 응답 정의
│   └── interceptor.ts     ← 요청 가로채기 로직
├── ui/
│   ├── button.tsx         ← 공통 버튼
│   ├── custom-input.tsx   ← 공통 입력 필드
│   └── ...
├── constants/
│   └── theme.ts           ← 색상, 폰트 등 디자인 토큰
└── assets/                ← 이미지, 폰트 파일
```

---

## 6. 코드 작성 규칙 — 어디에 무엇을 넣는가

### 판단 기준 요약

| 상황 | 어디에 넣을지 |
|------|-------------|
| 서버에서 가져오는 데이터 (계좌, 유저, 결제내역) | `entities/` |
| 사용자가 하는 행동 (로그인, 이체, 설정 변경) | `features/` |
| 여러 페이지에서 쓰이는 큰 UI 블록 | `widgets/` |
| 특정 화면의 레이아웃 | `pages/` |
| 버튼, 인풋 같은 원자 UI | `shared/ui/` |
| API 클라이언트, 유틸 함수 | `shared/` |

### 파일 내부 구조 규칙

각 폴더는 반드시 `index.ts`를 통해서만 외부에 공개합니다.

```typescript
// entities/payment/index.ts
export { getPaymentsApi } from './api/get-payments';
export { usePayments } from './model/use-payments';
export type { Transaction, TransactionType } from './types/payment';
```

다른 곳에서 import 할 때:
```typescript
// Good ✅
import { usePayments } from '@/entities/payment';

// Bad ❌ (내부 구조에 직접 접근하지 마세요)
import { usePayments } from '@/entities/payment/model/use-payments';
```

---

## 7. API 작업 가이드 — entities vs features

가장 헷갈리는 부분입니다. 아래 기준으로 판단하세요.

### entities에 API를 넣는 경우

**"이 데이터가 앱의 핵심 도메인 모델인가?"**

→ 결제내역, 계좌 정보, 사용자 프로필처럼 **데이터 자체**가 중요한 경우

```
entities/payment/api/get-payments.ts      ← 결제내역 조회
entities/user-profile/api/get-profile.ts  ← 프로필 조회
entities/account/api/get-account.ts       ← 계좌 조회
```

**패턴:**
```typescript
// entities/payment/api/get-payments.ts
import { apiClient } from '@/shared/api';
import type { GetPaymentsRequest, GetPaymentsResponse } from '../types/payment';

export async function getPaymentsApi(params: GetPaymentsRequest): Promise<GetPaymentsResponse> {
  const response = await apiClient.get<GetPaymentsResponse>(
    `/accounts/${params.accountId}/transactions`,
    { params }
  );
  return response.data;
}
```

### features에 API를 넣는 경우

**"이 API 호출이 사용자의 특정 행동(Use Case)과 묶여 있는가?"**

→ 로그인, 로그아웃, 송금 요청, 비밀번호 변경처럼 **행동**이 중심인 경우

```
features/auth/api/login.ts          ← 로그인 요청
features/auth/api/logout.ts         ← 로그아웃 요청
features/transfer/api/transfer.ts   ← 송금 요청
```

**패턴:**
```typescript
// features/auth/api/login.ts
import { apiClient } from '@/shared/api';
import type { LoginRequest, LoginResponse } from '../types/login';

export async function loginApi(body: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/auth/login', body);
  return response.data;
}
```

### 한눈에 비교

| 구분 | entities | features |
|------|----------|----------|
| 질문 | "어떤 데이터야?" | "무슨 행동이야?" |
| HTTP 메서드 | 주로 GET | POST, PUT, DELETE |
| 예시 | 결제내역 조회, 계좌 조회 | 로그인, 송금, 설정 변경 |
| 모델(hook) | Zustand store + hook | useCallback 중심의 action hook |

### 실제 API 추가 순서 (entities 예시)

새 도메인 `account-detail`의 상세 조회 API를 추가한다고 가정:

```bash
# 1. 폴더 구조 생성
entities/
└── account-detail/
    ├── api/
    │   └── get-account-detail.ts
    ├── model/
    │   └── use-account-detail.ts
    ├── types/
    │   └── account-detail.ts
    └── index.ts
```

```typescript
// 2. 타입 정의 (types/account-detail.ts)
export interface AccountDetail {
  accountId: number;
  bankName: string;
  balance: number;
  accountNumber: string;
}

export interface GetAccountDetailResponse {
  status: string;
  data: AccountDetail;
}
```

```typescript
// 3. API 함수 (api/get-account-detail.ts)
import { apiClient } from '@/shared/api';
import type { GetAccountDetailResponse } from '../types/account-detail';

export async function getAccountDetailApi(accountId: number): Promise<GetAccountDetailResponse> {
  const response = await apiClient.get<GetAccountDetailResponse>(`/accounts/${accountId}`);
  return response.data;
}
```

```typescript
// 4. 훅 (model/use-account-detail.ts)
import { useState, useEffect } from 'react';
import { getAccountDetailApi } from '../api/get-account-detail';
import type { AccountDetail } from '../types/account-detail';

export function useAccountDetail(accountId: number | null) {
  const [detail, setDetail] = useState<AccountDetail | null>(null);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!accountId) return;
    setIsPending(true);
    getAccountDetailApi(accountId)
      .then((res) => setDetail(res.data))
      .catch(() => setError('계좌 정보를 불러오지 못했습니다.'))
      .finally(() => setIsPending(false));
  }, [accountId]);

  return { detail, isPending, error };
}
```

```typescript
// 5. index.ts (외부 공개)
export { getAccountDetailApi } from './api/get-account-detail';
export { useAccountDetail } from './model/use-account-detail';
export type { AccountDetail } from './types/account-detail';
```

---

## 8. 새 화면·기능 추가 실전 순서

### 예시: "이체 내역 상세" 화면 추가

#### Step 1 — 라우트 파일 추가

```typescript
// app/(tabs)/transfer-detail.tsx
import { TransferDetailPage } from '@/pages/transfer-detail';

export default function TransferDetailScreen() {
  return <TransferDetailPage />;
}
```

#### Step 2 — pages 레이어에 화면 컴포넌트 추가

```
pages/transfer-detail/
├── ui/
│   ├── transfer-detail-page.tsx
│   └── transfer-detail-page.styles.ts
└── index.ts
```

```typescript
// pages/transfer-detail/ui/transfer-detail-page.tsx
import { View } from 'react-native';
import { ThemedText } from '@/shared/ui';
import { useTransferDetail } from '@/entities/transfer';  // entities에서 가져옴

export function TransferDetailPage() {
  const { detail } = useTransferDetail();
  return (
    <View>
      <ThemedText>{detail?.amount}</ThemedText>
    </View>
  );
}
```

#### Step 3 — 필요한 데이터를 entities에 추가

위 "API 추가 순서" 섹션 참고.

#### Step 4 — 화면 이동 (라우팅)

```typescript
import { router } from 'expo-router';

// 다른 화면으로 이동
router.push('/transfer-detail');

// 뒤로 가기
router.back();

// 현재 화면 교체 (뒤로 가기 불가)
router.replace('/home');
```

---

## 9. Mock 데이터 사용법

실제 서버 없이 개발할 수 있도록 Mock 인터셉터가 구현되어 있습니다.

### Mock 켜기/끄기

```env
# front/.env
EXPO_PUBLIC_IS_MOCK=true   ← 이 값이 true면 Mock 사용
EXPO_PUBLIC_IS_MOCK=false  ← 실제 서버 사용
```

> .env 수정 후에는 `npm start`를 재시작해야 합니다.

### Mock 핸들러 추가

새 API를 만들었는데 서버가 아직 없을 때:

```typescript
// shared/mock/handlers.ts
export const mockHandlers: MockHandler[] = [
  // ... 기존 핸들러들 ...

  // 새로 추가
  {
    method: 'GET',
    url: '/accounts/:accountId',  // :accountId는 어떤 값이든 매칭됨
    response: {
      status: 'SUCCESS',
      data: {
        accountId: 42,
        bankName: '돈땃쥐 은행',
        balance: 500000,
        accountNumber: '123-456-789',
      },
    },
  },
];
```

### Mock 데이터 JSON 파일 추가

응답 데이터가 길면 별도 JSON 파일로 분리:

```
shared/mock/data/
├── bank-account.json
├── payment-history.json
└── account-detail.json  ← 새로 추가
```

```typescript
// handlers.ts에서 import
import accountDetail from './data/account-detail.json';

{
  method: 'GET',
  url: '/accounts/:accountId',
  response: accountDetail,
}
```

---

## 10. 자주 쓰는 명령어 모음

```bash
# 개발 서버 시작 (기본)
npm start

# 안드로이드 에뮬레이터로 시작
npm run android

# iOS 시뮬레이터로 시작 (Mac 전용)
npm run ios

# 웹 브라우저로 시작
npm run web

# 타입 오류 확인
npm run check-types

# 린트 검사
npm run lint

# 타입 + 린트 한 번에
npm run test-all

# 캐시 초기화 후 재시작 (오류 발생 시 시도)
npx expo start --clear
```

---

## 11. 트러블슈팅

### `npm install` 후 오류가 발생해요

```bash
# node_modules 삭제 후 재설치
rm -rf node_modules
npm install
```

### 앱이 업데이트가 안 돼요

Expo Go 앱에서 화면을 흔들기 → **Reload** 선택
또는 터미널에서 `r` 키 입력

### 캐시 관련 오류

```bash
npx expo start --clear
```

### 타입 오류가 많이 나요

```bash
npm run check-types
```

오류 목록을 확인하고 하나씩 해결하세요.

### import 경로 `@/` 가 동작하지 않아요

`tsconfig.json`의 paths 설정이 있는지 확인:
```json
{
  "compilerOptions": {
    "paths": {
      "@/*": ["./*"]
    }
  }
}
```

### SecureStore 관련 오류 (웹에서)

`expo-secure-store`는 웹에서 동작하지 않습니다. 실제 폰이나 에뮬레이터를 사용하세요.

### Mock이 동작하지 않아요

1. `.env` 파일에 `EXPO_PUBLIC_IS_MOCK=true` 확인
2. `npm start` 재시작
3. `shared/mock/handlers.ts`에 해당 URL이 등록되어 있는지 확인

---

## 참고 자료

- [Expo 공식 문서](https://docs.expo.dev)
- [Expo Router 문서](https://expo.github.io/router)
- [React Native 공식 문서](https://reactnative.dev)
- [FSD 아키텍처 가이드](https://feature-sliced.design)
- [Zustand 문서](https://zustand.docs.pmnd.rs)

---

> 궁금한 점은 팀 채널에 질문하세요!
