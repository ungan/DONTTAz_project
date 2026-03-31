# Logout Worklog - 2026-03-19

## 1. 목적

이 문서는 2026-03-19 기준 로그아웃 관련 프론트 수정 내용을 간단히 정리한 메모다.

## 2. 왜 수정했는가

- 테스트 중 예전 세션이 계속 남아서 회원가입, PIN 로그인, 재진입 흐름 확인이 불편했다.
- 금융 앱 특성상 로컬 세션 정리와 명시적인 로그아웃 동선이 필요했다.
- 로그아웃 버튼이 없어서 앱 삭제/재설치에 의존하는 상태를 줄이려 했다.

## 3. 어디에 붙였는가

- 로그아웃 버튼 위치는 `menu` 탭으로 정리했다.
- 홈보다 `전체/설정` 성격 화면에 두는 것이 자연스럽다고 판단했다.

## 4. 어떻게 수정했는가

### 4.1 API 추가

- `front/features/auth/api/logout.ts`
- `POST /auth/logout` 호출 함수 추가

### 4.2 타입 추가

- `front/features/auth/types/logout.ts`
- 로그아웃 요청/응답 타입 추가

### 4.3 로그아웃 훅 추가

- `front/features/auth/model/use-logout.ts`
- 로그아웃 버튼 클릭 시 실제 동작을 담당
- 처리 흐름:
  1. 저장된 access token / refresh token 확인
  2. access token이 있으면 서버 로그아웃 시도
  3. 이후 로컬 세션 정리
  4. `/login`으로 이동

### 4.4 로컬 세션 정리 함수 추가

- `front/features/auth/model/clear-local-auth-session.ts`
- 아래 정보를 한 번에 정리하도록 구성
  - access token
  - refresh token
  - 저장된 PIN
  - session store
  - bank-account store
  - runtime auth 상태

### 4.5 메뉴 화면 수정

- `front/pages/menu/ui/menu-page.tsx`
- `front/pages/menu/ui/menu-page.styles.ts`
- 로그아웃 버튼과 확인 모달 추가
- 공용 컴포넌트 재사용:
  - `shared/ui/button.tsx`
  - `shared/ui/confirm-modal.tsx`

## 5. 현재 동작 기준

- 메뉴 탭에서 `로그아웃` 버튼 노출
- 버튼 클릭 시 확인 모달 표시
- 확인 후 로그아웃 시도
- 성공 시 로그인 화면으로 이동

## 6. 예외 처리

- access token이 없는 상태라면 서버 로그아웃 호출 없이 로컬 로그아웃만 수행
- 서버가 `400` 또는 `401`을 반환해도 로컬 세션은 정리하고 로그인 화면으로 이동

## 7. 같이 손본 관련 흐름

- PIN 로그인 정식화를 위해 refresh API 연동 작업도 이어서 진행했다.
- 현재는 PIN 성공 후 바로 홈으로 가지 않고 refresh token으로 새 토큰 발급을 시도하는 방향으로 수정 중이다.

## 8. 확인한 파일

- `front/features/auth/api/logout.ts`
- `front/features/auth/types/logout.ts`
- `front/features/auth/model/use-logout.ts`
- `front/features/auth/model/clear-local-auth-session.ts`
- `front/pages/menu/ui/menu-page.tsx`
- `front/pages/menu/ui/menu-page.styles.ts`

## 9. 메모

- 현재 로그아웃 이슈는 프론트 버튼 자체보다 `access token 없는 상태`, `PIN 로그인 후 토큰 재발급 여부`, `백엔드 인증 정책`과 연결되어 있었다.
- 그래서 로그아웃만 단독으로 보기보다 로그인/PIN/refresh 흐름과 같이 봐야 한다.
