# 가상금고 구현 워크로그 - 2026-03-21

## 1. 문서 목적

2026-03-21에 진행한 가상금고 기능 구현 내용을 정리한다.
다시 봐도 바로 이해할 수 있도록 폴더 구조, 유저 흐름, 각 파일 역할을 명료하게 기술한다.

---

## 2. 전체 유저 흐름

```
(tabs)/vault
  └─ 가상금고 메인 페이지
       ├─ 잔액 확인 (VaultBalance)
       ├─ 넣기 버튼 → (미구현)
       ├─ 긴급 출금 버튼 → (미구현)
       ├─ 이자율 안내 (VaultNotice)
       ├─ 입출금 내역 (VaultHistory)
       └─ "위시리스트 ›" 누르면 → /vault-wishlist (슬라이드 애니메이션)

/vault-wishlist
  └─ 위시리스트 페이지
       ├─ 위시리스트 없음 → empty state + "등록하기" 버튼
       ├─ 위시리스트 있음 → 카드 peek 슬라이더
       ├─ + 버튼 누르면 → WishlistBottomSheet (추가 모드)
       └─ 카드 수정 버튼 → WishlistBottomSheet (수정 모드)

WishlistBottomSheet
  ├─ 무엇을 살 건가요? (텍스트 입력)
  ├─ 목표 금액 (숫자 입력)
  ├─ 목표 날짜 → 누르면 인라인 캘린더 표시 (react-native-calendars)
  └─ 저장 버튼 → API 호출 후 목록 자동 갱신
```

---

## 3. 폴더 구조

FSD(Feature-Sliced Design) 아키텍처 기준으로 구성했다.
계층 규칙: `pages → widgets → features → entities → shared` (아래 계층만 import 가능)

```
front/
├── app/
│   ├── (tabs)/vault.tsx          # 가상금고 탭 라우트
│   └── vault-wishlist.tsx        # 위시리스트 스택 라우트 (headerShown: false)
│
├── pages/
│   ├── vault/
│   │   └── ui/vault-page.tsx     # 가상금고 메인 페이지 조합
│   └── vault-wishlist/
│       └── ui/vault-wishlist-page.tsx  # 위시리스트 페이지 조합
│
├── widgets/
│   ├── vault-balance/
│   │   └── ui/vault-balance.tsx  # 잔액 카드 (이미지 + 버튼 + 위시리스트 링크)
│   ├── vault-notice/
│   │   └── ui/vault-notice.tsx   # 이자율 안내 섹션
│   ├── vault-history/
│   │   └── ui/vault-history.tsx  # 입출금 내역 FlatList
│   └── vault-carousel/
│       └── ui/vault-carousel.tsx # (현재 미사용) 슬라이더 형태 대안
│
├── features/
│   └── vault-wishlist/
│       ├── model/use-wishlist-form.ts        # 폼 상태 + 유효성 검사
│       └── ui/wishlist-bottom-sheet.tsx      # 위시리스트 추가/수정 바텀시트
│
├── entities/
│   └── vault/
│       ├── types/vault.ts         # 타입 정의
│       ├── api/
│       │   ├── get-vault.ts
│       │   ├── get-vault-transactions.ts
│       │   ├── get-wishlist.ts
│       │   ├── post-wishlist.ts
│       │   └── patch-wishlist.ts
│       └── model/
│           ├── use-vault.ts       # 금고 + 내역 조회 훅
│           ├── use-wishlist.ts    # 위시리스트 조회 + 등록/수정 훅
│           └── wishlist-store.ts  # Zustand 위시리스트 상태
│
└── shared/
    ├── assets/images/
    │   └── vault-and-chain.png    # 금고 + 체인 합성 이미지 (219×132)
    ├── mock/
    │   ├── data/vault.json
    │   ├── data/vault-transactions.json   # 50건 (최신순, 첫 건은 DEPOSIT)
    │   ├── data/vault-wishlist.json
    │   └── handlers.ts            # mock interceptor에 vault 경로 추가
    └── ui/
        └── bottom-sheet-base.tsx  # 공용 바텀시트 래퍼
```

---

## 4. 핵심 파일별 역할

### entities/vault/types/vault.ts
API 응답 타입을 정의한다. 주요 타입은 아래와 같다.

| 타입 | 설명 |
|---|---|
| `Vault` | 금고 정보 (잔액, 이자율, 상태 등) |
| `VaultTransaction` | 개별 입출금 내역 |
| `Wishlist` | 위시리스트 아이템 (id, wishName, goalAmount, targetDate) |
| `WishlistRequest` | 위시리스트 등록/수정 요청 바디 |

### entities/vault/model/use-vault.ts
`GET /api/v1/vaults`를 호출해 `vault`와 `transactions`를 반환한다.
transactions는 `createdAt` 내림차순(최신순)으로 정렬한다.

### entities/vault/model/use-wishlist.ts
`GET /api/v1/vaults/:vaultId/wishlist`로 목록을 조회하고 Zustand store에 저장한다.
`postWishlist` / `patchWishlist` 호출 후 자동으로 목록을 다시 불러온다.

### features/vault-wishlist/model/use-wishlist-form.ts
바텀시트 폼 상태를 관리한다.
- `editTarget`(수정 대상)을 받으면 기존 값으로 초기화한다.
- `isValid`: wishName 있고 goalAmount > 0 이어야 true
- `getRequestBody()`: API 요청 바디 반환 (targetDate는 `YYYY-MM-DD` 문자열)

### shared/ui/bottom-sheet-base.tsx
`@gorhom/bottom-sheet`를 래핑한 공용 컴포넌트.
- `keyboardBehavior="extend"` — 키보드 올라오면 시트도 같이 올라옴
- `keyboardBlurBehavior="restore"` — 키보드 내려가면 원래 위치로 복구
- `BottomSheetScrollView` + `keyboardShouldPersistTaps="handled"` 사용

---

## 5. API 엔드포인트 정리

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/v1/vaults` | 내 금고 정보 조회 |
| GET | `/api/v1/vaults/:vaultId/history` | 입출금 내역 조회 |
| GET | `/api/v1/vaults/:vaultId/wishlist` | 위시리스트 목록 조회 |
| POST | `/api/v1/vaults/:vaultId/wishlist` | 위시리스트 추가 |
| PATCH | `/api/v1/vaults/:vaultId/wishlist/:wishId` | 위시리스트 수정 |

---

## 6. 주요 결정 사항

### 날짜 선택 라이브러리
- `@react-native-community/datetimepicker` → 네이티브 모듈이라 `eas build` 필요
- **`react-native-calendars`로 교체** — Pure JS, `eas update`만으로 동작

### 위시리스트 UX 구조
- 처음엔 VaultBalance 아래 캐로셀(슬라이더)로 위시리스트를 붙이려 했음
- **별도 페이지(`/vault-wishlist`)로 분리** — UX 더 명확하고 스크롤 충돌 없음
- VaultBalance 우측 상단 "위시리스트 ›" 누르면 슬라이드 애니메이션으로 이동

### 이미지 처리
- vault + chain을 각각 배치하려다 버튼이 컨테이너 밖으로 벗어나는 문제 발생
- **`vault-and-chain.png` 단일 이미지(219×132)로 통일**

---

## 7. 미구현 항목

| 항목 | 비고 |
|---|---|
| 넣기(입금) 페이지 | `onDeposit` 핸들러 비어있음 |
| 긴급 출금 모달 | `onWithdraw` 핸들러 비어있음 |
| 위시리스트 진행률 표시 | 카드에 목표 대비 현재 잔액 % 표시 예정 |
