# 회원가입 UI 개선 작업 로그 (2026-03-23)

## 환경 설정

### BE .env 수정
- `DB_URL` 값에 `&` 포함으로 bash source 시 변수 미설정 문제 → 값 전체 `"..."` 로 감싸 해결
- DB 스키마명 `스키마이름` → `test_cji` 수정
- 핫스팟 환경 IP 변경: `192.168.35.193` → `172.20.10.13` (주석으로 집 와이파이 보존)

### FE .env 수정
- `EXPO_PUBLIC_BASE_URL` 핫스팟 IP로 변경

---

## Mock 설정 변경

### 실제 API 요청으로 전환 (mock handlers에서 제거)
- `/auth/login` — 로그인
- `/auth/verify-identity` — 본인인증
- `/users/signup` — 회원가입
- `/users/me` — 사용자 정보
- `/accounts/demand-deposits/products` — 계좌 상품 목록
- `/accounts` POST — 계좌 생성

### PortOne 본인인증 실제 호출로 변경
- `use-signup-flow.ts` `startVerification`: `IS_MOCK` 조건 제거, 항상 `requestPortOneIdentityVerification()` 호출

---

## UI 개선

### PageHeader — Safe Area 대응
- `useSafeAreaInsets` 추가, `paddingTop: insets.top + 12` 적용
- `paddingVertical: 12` → `paddingBottom: 12` 로 변경
- PageHeader 사용하는 18개 페이지 전체 자동 적용

### `_layout.tsx`
- `SafeAreaProvider` 루트에 추가 (Android 하단 네비게이션 바 대응)

---

## 회원가입 Progress Bar

- 4개 분리된 바 → 하나의 연속된 바 (1/4씩 채워짐)
- 라벨(본인인증/계정설정/PIN설정/완료) 제거 → 각 PageHeader title로 이동
- 헤더와 바 사이 gap 제거, 바를 화면 양 끝까지 확장 (`marginHorizontal: -16`)
- `paddingTop` 제거 후 `paddingBottom`만 유지

---

## 각 단계별 UI 개선

### 본인인증 페이지 (`signup-start`)
- "오늘 구현 범위" 카드 제거
- 하단 helperText 제거
- 내용: "휴대폰 본인인증" 타이틀 + 인증 시 확인 정보 카드 (이름/생년월일/휴대폰)
- 버튼 텍스트: "본인인증 시작" → "휴대폰 본인인증"

### 계정설정 페이지 (`signup-account`)
- 타이틀: "이메일·비밀번호 설정"
- description: 기술적 표현 제거
- form helperText: "백엔드 규칙에 맞춰..." → "영문, 숫자, 특수문자를 포함한 8~16자로 입력해 주세요."
- **버튼 하단 고정**: `ScrollView` + 외부 `View(bottomBar)` 구조로 변경, `useSafeAreaInsets` 적용

### PIN 설정 페이지 (`signup-pin`)
- `ScrollView` → `View(flex:1)` 전체 화면 구조로 변경
- `SignupVerifiedSummary`, description, 버튼 2개, helperText 제거
- 인디케이터 2개 동시 표시 → 단계별 1개만 표시 (`isConfirming` 기준)
- 포인트 컬러(`#5899FD`) dot 적용
- dot 크기: 14 → 18
- 확인 PIN 6자리 완성 시 자동으로 다음 단계 진행 (`useEffect`)
- 타이틀/도트 상단 중앙, 키패드 하단 고정 (`justifyContent: 'space-between'`)

### 완료 페이지 (`signup-complete`)
- 자동 진행 제거 → "완료" 버튼 누를 때 가입+로그인 처리
- **버튼 하단 고정**: `ScrollView` + 외부 `View(bottomBar)` 구조, `useSafeAreaInsets` 적용
- 체크 아이콘: `Ionicons` → `checked.png` (88x88) 커스텀 이미지
- "회원가입이 완료되었습니다" 한 줄 표시
- 요약 정보: 이름/휴대폰/이메일만 표시 (비밀번호/PIN 제거)
- 요약 카드 → 위아래 선(1.5pt, 검정)만 있는 리스트 스타일
- `SignupVerifiedSummary` 타이틀: "인증된 본인 정보" → "✓ 본인인증 완료" (초록색)

---

## 버그 수정

### 회원가입 완료 후 본인인증 화면으로 돌아오는 문제
- 원인: `completeLoginSession`(홈 이동) 후 `resetFlow()` 호출 → `isVerified=false` → useEffect가 `/signup-start` 리다이렉트
- 해결: `resetFlow()` 제거, `completeLoginSession`을 마지막으로 이동

---

## 타이틀 통일 (명사형)

| 페이지 | Header title | 본문 title |
|--------|-------------|-----------|
| signup-start | 본인인증 | 휴대폰 본인인증 |
| signup-account | 계정설정 | 이메일·비밀번호 설정 |
| signup-pin | PIN설정 | 간편 PIN 설정 |
| signup-complete | 완료 | 회원가입이 완료되었습니다 |
