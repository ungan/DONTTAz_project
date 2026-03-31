# Auth Worklog - 2026-03-18

## 1. 문서 목적

이 문서는 2026-03-18에 진행한 인증 관련 작업을 정리하고, 다음 작업자가 바로 이어서 볼 수 있도록 현재 상태와 남은 이슈를 남기기 위한 메모다.

우선 참고 기준은 아래 순서를 따른다.

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`

같은 내용이 겹치면 `FRONTEND_GUIDE.md`를 우선으로 보고, 화면 구성과 공용 UI 사용 기준은 `design-guide.md`를 따른다.

## 2. 오늘 한 작업 요약

### 2.1 회원가입 흐름 정리

- PortOne 본인인증 기반 회원가입 4단계 MVP를 프론트에 연결했다.
- 흐름은 `signup-start -> signup-account -> signup-pin -> signup-complete` 기준이다.
- 회원가입 성공 후 자동 로그인까지 연결했다.
- 로그인 화면에서 회원가입 진입도 연결했다.

### 2.2 PortOne 디버깅

- `test-auth.html` 기반으로 PortOne 브라우저 SDK 테스트 흐름을 붙였다.
- `crypto.randomUUID()` 미지원 환경을 위해 fallback을 넣었다.
- callback URL을 앱 딥링크 기준으로 정리했다.
- 자동 복귀가 막힐 때를 대비해 `앱으로 돌아가기` 버튼도 테스트 페이지에 추가했다.

### 2.3 로그인 정리

- 로그인 성공 후 홈 탭 진입 경로를 `/(tabs)/home` 기준으로 정리했다.
- 스플래시에서 access token 존재 여부를 보고 로그인 상태를 판단하도록 수정했다.
- axios 요청 시 access token을 자동으로 붙이도록 정리했다.
- 로그인 실패 메시지를 백엔드 응답 기준으로 보여주도록 보완했다.

### 2.4 PIN 진입 MVP 추가

- 회원가입 시 설정한 PIN을 기기에 저장하도록 처리했다.
- 앱 재실행 시 `refresh token + 저장된 PIN`이 있으면 `pin-login` 화면으로 보내는 MVP 흐름을 추가했다.
- PIN 성공 시 홈으로 보내고, 다른 계정 로그인 선택 시 로컬 세션을 비우고 일반 로그인으로 보내도록 구성했다.

### 2.5 로그아웃 MVP 추가

- 로그아웃 API 호출용 `features/auth/api/logout.ts`를 추가했다.
- 로그아웃 훅 `use-logout.ts`를 만들었다.
- 로그아웃 버튼은 `menu` 탭에 배치했다.
- 메뉴 화면은 `pages/menu`에서 구성하고, 실제 동작은 `features/auth` 훅이 담당하도록 분리했다.
- 로그아웃 시 아래를 정리하도록 구현했다.
  - access token
  - refresh token
  - 저장된 PIN
  - session store
  - bank-account store

## 3. 오늘 변경한 핵심 파일

### 3.1 회원가입 / PortOne

- `front/features/signup/api/portone.ts`
- `front/features/signup/model/use-signup-flow.ts`
- `front/pages/signup-start/ui/signup-start-page.tsx`
- `donttaz-BE/api/src/main/resources/static/test-auth.html`

### 3.2 로그인 / 세션

- `front/features/auth/model/complete-login-session.ts`
- `front/features/auth/model/use-auth-check.ts`
- `front/features/auth/model/use-login.ts`
- `front/shared/api/axios-instance.ts`
- `front/shared/api/auth-storage.ts`
- `front/pages/splash/ui/splash-page.tsx`
- `front/app/(tabs)/_layout.tsx`
- `front/app/_layout.tsx`

### 3.3 PIN 진입

- `front/features/auth/model/use-app-entry-check.ts`
- `front/features/auth/model/auth-runtime-store.ts`
- `front/pages/pin-login/ui/pin-login-page.tsx`
- `front/pages/pin-login/ui/pin-login-page.styles.ts`
- `front/pages/pin-login/index.ts`
- `front/app/pin-login.tsx`

### 3.4 로그아웃

- `front/features/auth/api/logout.ts`
- `front/features/auth/model/use-logout.ts`
- `front/features/auth/model/clear-local-auth-session.ts`
- `front/features/auth/types/logout.ts`
- `front/pages/menu/ui/menu-page.tsx`
- `front/pages/menu/ui/menu-page.styles.ts`

## 4. 현재 동작 기준

### 4.1 회원가입

- PortOne 인증 성공 후 회원가입 단계로 이동 가능
- 계정 정보 입력 가능
- PIN 설정 가능
- 가입 성공 후 자동 로그인 가능

### 4.2 로그인

- 일반 로그인 가능
- 로그인 성공 후 홈 탭 이동 가능

### 4.3 로그아웃

- 메뉴 탭에서 로그아웃 버튼 노출
- 확인 모달 후 로그아웃 시도
- 서버 로그아웃이 실패해도 400/401이면 로컬 세션 정리 후 로그인 화면 이동

### 4.4 앱 재진입 정책

현재 의도한 정책은 아래와 같다.

- refresh token + 저장된 PIN 있음 -> PIN 로그인
- refresh token 없음 -> 일반 로그인

다만 이 정책은 아직 MVP 수준이며, 정식으로 완성된 상태는 아니다.

## 5. 현재 남아 있는 핵심 이슈

### 5.1 PortOne callback 중복 처리 흔적

- 한 번의 인증에 대해 백엔드 로그가 두 번 찍히는 구간이 있었다.
- `openAuthSessionAsync` 결과 처리와 deep link fallback 처리가 동시에 타면서 중복 검증이 생겼을 가능성이 높다.
- 현재 회원가입은 동작하지만, callback 처리 중복 여부는 다시 확인이 필요하다.

### 5.2 PIN 로그인의 정식화 미완료

현재 PIN 로그인은 로컬 PIN 일치 여부만 보고 홈으로 보낸다.

즉 현재 구조는:

- PIN 성공
- 바로 홈 이동

정식 방향은:

- PIN 성공
- refresh token 기반 access token 재발급
- 새 토큰 저장
- 홈 이동

이어야 한다.

### 5.3 로그아웃 백엔드 인증 이슈

백엔드 로그 기준으로 `/auth/logout` 호출 시 `인증이 필요합니다.`가 발생했다.

원인 정리:

- 로그아웃은 refresh token만으로 되는 구조가 아님
- 유효한 access token 인증이 같이 필요함
- 현재 PIN 로그인은 access token 재발급 없이 홈으로 보내므로, 오래된 세션에서는 logout 시 access token 인증이 실패할 수 있음

즉 지금 로그아웃은 프론트 로컬 세션 정리 용도로는 동작하지만, 백엔드 관점의 정식 로그아웃은 아직 불완전하다.

## 6. 백엔드에 요청해야 할 내용

### 6.1 꼭 필요한 API

정식 PIN 로그인 개선을 위해 아래 API가 필요하다.

- `POST /api/v1/auth/refresh`
또는
- `POST /api/v1/auth/reissue`

요청 예시:

```json
{
  "refreshToken": "string"
}
```

응답은 가능하면 로그인 응답과 같은 shape가 좋다.

```json
{
  "status": "SUCCESS",
  "message": "토큰이 재발급되었습니다.",
  "data": {
    "accessToken": "new-access-token",
    "refreshToken": "new-refresh-token-or-existing-one",
    "userId": 1,
    "name": "김회인"
  }
}
```

### 6.2 백엔드 확인 포인트

- refresh token 유효성 검증 방식
- Redis 저장 refresh token과의 일치 여부
- refresh 성공 시 access token만 재발급할지, refresh token도 rotate할지
- PIN 로그인 성공 후 프론트가 추가로 필요한 사용자 정보가 있는지

## 7. 내일 바로 할 일

우선순위 순서대로 정리하면 아래가 맞다.

### 7.1 1순위

- 백엔드에 refresh/reissue API 필요사항 공유
- 현재 백엔드에 refresh API 추가 가능 여부 확인

### 7.2 2순위

- PIN 로그인 정식 흐름 설계
- `PIN 성공 -> refresh API 호출 -> 새 토큰 저장 -> 홈 이동`으로 프론트 구조 변경

### 7.3 3순위

- 로그아웃을 refresh 기반 구조에 맞게 재검증
- access token이 만료된 상태에서도 기대한 UX로 빠지는지 확인

### 7.4 4순위

- PortOne callback 중복 호출 여부 재테스트
- 회원가입 첫 진입부터 자동 로그인, 앱 재실행, PIN 로그인, 로그아웃까지 한 사이클 점검

## 8. 내일 작업 시작 전에 확인할 것

### 8.1 프론트

- `front/.env`
- `EXPO_PUBLIC_BASE_URL` 값
- `EXPO_PUBLIC_IS_MOCK` 값
- dev build 앱으로 테스트 중인지

### 8.2 백엔드

- 서버 실행 상태
- `/auth/logout` 인증 필요 정책 유지 여부
- refresh API 존재 여부

### 8.3 테스트 기준

내일 테스트는 아래 순서로 보는 것이 좋다.

1. 새 회원가입
2. 자동 로그인
3. 앱 완전 종료
4. 앱 재실행
5. PIN 로그인
6. 홈 진입
7. 로그아웃
8. 로그인 화면 복귀

## 9. 메모

- 지금 상태는 “회원가입/로그인/로그아웃/PIN 진입 MVP가 모두 얼개는 붙은 상태”로 보면 된다.
- 다만 금융 앱 기준으로는 PIN 성공 후 refresh token 기반 access token 재발급이 꼭 필요하다.
- 내일은 UI보다 인증 정책 정식화가 우선이다.
