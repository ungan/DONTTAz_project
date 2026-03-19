# Signup Worklog - 2026-03-17

## 1. 문서 목적

이 문서는 2026-03-17에 정리한 회원가입/로그인 플로우 논의를 내일 바로 이어서 작업할 수 있도록 정리한 작업 메모다.

우선 참고 문서 순서는 아래와 같다.

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`

같은 내용이 있으면 `FRONTEND_GUIDE.md`를 먼저 따른다.
디자인 톤, 공용 스타일, 화면 일관성은 `design-guide.md`를 참고한다.

## 2. 오늘 확정한 큰 방향

### 2.1 서버 연결 방향

- 휴대폰 테스트 기준으로 진행한다.
- 백엔드 서버는 Swagger 접속 기준 `8080` 포트를 사용 중인 것으로 본다.
- API 연결은 `.env`의 `EXPO_PUBLIC_BASE_URL`을 기준으로 한다.
- 로컬/실서버 전환은 `EXPO_PUBLIC_IS_MOCK`와 `EXPO_PUBLIC_BASE_URL`로 관리한다.
- 휴대폰 테스트 시 `localhost`가 아니라 PC의 로컬 IP를 사용해야 한다.

예시:

```env
EXPO_PUBLIC_BASE_URL=http://192.168.100.66:8080/api/v1
EXPO_PUBLIC_IS_MOCK=false
```

### 2.2 회원가입/로그인 정책

- 회원가입 성공: 현재 백엔드 기준 `200 OK`
- 로그인 성공: `200 OK`
- 형식 오류: `400 Bad Request`
- 아이디/전화번호 중복: `409 Conflict`
- 로그인 실패: `401 Unauthorized`
- `pinCode`는 회원가입 시 함께 설정한다.

### 2.3 회원가입 후 흐름

처음에는 회원가입 후 로그인 화면으로 보내는 방향도 검토했지만, 최종적으로 아래 흐름으로 정리했다.

- 회원가입 성공
- 자동 로그인 진행
- 자동 로그인 성공
- 홈 진입

이후 앱 재실행 시에는 아래 흐름을 목표로 한다.

- refresh token이 있으면 PIN 기반 간편 진입
- refresh token이 없거나 만료되면 일반 로그인 화면 이동

주의:

- PIN은 "정식 로그인"보다 "이미 인증된 기기에서의 간편 재인증" 개념으로 보는 것이 적절하다.
- 구현 순서는 자동 로그인 먼저, PIN 간편 진입은 그다음 단계로 가는 것이 안정적이다.

## 3. 회원가입 구조 방향

### 3.1 기존 생각에서 수정된 점

처음에는 `name`을 직접 입력하는 회원가입 흐름을 고려했지만, 이후 PortOne 본인인증 기반으로 구조가 바뀔 가능성이 높다고 판단했다.

따라서 회원가입은 아래 구조를 기준으로 설계한다.

- 본인인증 시작
- PortOne 인증 완료
- 인증 결과로 사용자 기본정보 확보
- 사용자가 직접 설정할 계정정보 입력
- PIN 설정
- 회원가입 API 호출
- 자동 로그인
- 홈 진입

### 3.2 값 구분

본인인증으로 받을 가능성이 큰 값:

- `name`
- `phoneNumber`
- 필요 시 생년월일 등

사용자가 직접 입력해야 하는 값:

- `loginId`
- `password`
- `pinCode`

즉, 프론트 구조상 `name`과 `phoneNumber`는 "직접 입력값"이 아니라 "인증 결과값"으로 다루는 방향을 우선한다.

## 4. 실제 구현 화면 목록

오늘 논의 기준으로 MVP 화면은 아래 4개를 추천했다.

1. `signup-start`
2. `signup-account`
3. `signup-pin`
4. `signup-complete`

### 4.1 signup-start

역할:

- 회원가입 시작 화면
- 본인인증 시작 안내
- PortOne 인증 진입

### 4.2 signup-account

역할:

- 사용자가 직접 입력할 계정정보 설정

포함 필드:

- `loginId`
- `password`
- `passwordConfirm`

### 4.3 signup-pin

역할:

- `pinCode`
- `pinCodeConfirm`
- PIN 설정 및 재입력 확인

### 4.4 signup-complete

역할:

- 회원가입 완료 상태 표시
- 자동 로그인 처리
- 성공 시 홈 진입

## 5. 화면별 핵심 동작 정리

### 5.1 signup-start

- 입력값 없음
- `본인인증 시작` 버튼 클릭
- PortOne V2 SDK callback 응답에서 `identityVerificationId` 확보
- `identityVerificationId`로 `/auth/verify-identity` 호출
- 인증 성공 시 `verifiedName`, `verifiedPhoneNumber`, `isVerified=true` 저장
- 실패 시 현재 화면에서 에러 메시지 노출

### 5.2 signup-account

- `loginId`, `password`, `passwordConfirm` 입력
- 비밀번호 규칙 검사
- 재입력 일치 검사
- 아이디 형식/중복 검사
- 유효하면 다음 단계로 이동

### 5.3 signup-pin

- `pinCode` 입력
- `pinCodeConfirm` 재입력
- 6자리 숫자 확인
- 일치 시 완료 단계로 이동

### 5.4 signup-complete

- 회원가입 API 호출
- 성공 시 로그인 API 자동 호출
- 로그인 성공 시 홈 이동
- 실패 시 에러 메시지 처리

## 6. 회원가입 플로우 상태 설계

공통 상태:

- `currentStep`
- `isSubmitting`
- `errorMessage`

인증 결과 상태:

- `verifiedName`
- `verifiedPhoneNumber`
- `isVerified`

계정 입력 상태:

- `loginId`
- `password`
- `passwordConfirm`

PIN 상태:

- `pinCode`
- `pinCodeConfirm`

화면별 보조 상태:

- `loginIdError`
- `passwordError`
- `passwordConfirmError`
- `pinError`
- `isStartingVerification`
- `isSigningUp`
- `isAutoLoggingIn`

## 7. 파일 구조 설계

`FRONTEND_GUIDE.md` 기준으로 `app -> pages -> features -> shared` 구조를 따른다.

추천 구조:

```txt
front/
├── app/
│   ├── signup-start.tsx
│   ├── signup-account.tsx
│   ├── signup-pin.tsx
│   └── signup-complete.tsx
│
├── pages/
│   ├── signup-start/
│   ├── signup-account/
│   ├── signup-pin/
│   └── signup-complete/
│
├── features/
│   └── signup/
│       ├── api/
│       │   └── signup.ts
│       ├── model/
│       │   ├── use-signup-flow.ts
│       │   └── signup-store.ts
│       ├── types/
│       │   └── signup.ts
│       ├── ui/
│       │   ├── signup-progress.tsx
│       │   ├── signup-account-form.tsx
│       │   └── signup-verified-summary.tsx
│       └── index.ts
```

핵심 원칙:

- `app`: 라우트 진입만 담당
- `pages`: 화면 단위 조합
- `features/signup`: 회원가입 로직과 기능 UI
- `shared`: 공용 UI 재사용

## 8. 공용 컴포넌트 재사용 기준

기존 코드에서 우선 재사용 검토할 파일:

- `shared/ui/button.tsx`
- `shared/ui/custom-input.tsx`
- `shared/ui/page-header.tsx`
- `shared/ui/pin-indicator.tsx`
- `shared/ui/numeric-keypad.tsx`

원칙:

- 버튼/입력/PIN UI는 새로 만들기보다 기존 공용 컴포넌트 재사용 우선
- 스타일은 `design-guide.md`의 일관성 기준 유지
- 인라인 스타일 남발하지 않기

## 9. 구현 순서 추천

가장 추천한 실제 구현 순서는 아래와 같다.

1. `features/signup/types/signup.ts`
2. `features/signup/api/signup.ts`
3. `features/signup/model/signup-store.ts`
4. `features/signup/model/use-signup-flow.ts`
5. `features/signup/ui/*`
6. `pages/signup-start`
7. `pages/signup-account`
8. `pages/signup-pin`
9. `pages/signup-complete`
10. `app/signup-*.tsx`
11. 로그인 화면에서 회원가입 이동 연결
12. 회원가입 성공 후 자동 로그인 연결

이 순서가 좋은 이유:

- 타입이 먼저 잡혀서 흔들림이 적다.
- API와 상태 구조가 먼저 생겨서 페이지가 얇아진다.
- `FRONTEND_GUIDE.md`의 레이어 구조와 잘 맞는다.
- `design-guide.md`의 화면 일관성도 지키기 쉽다.

## 10. 내일 바로 시작할 일

내일 작업 시작 순서는 아래를 추천한다.

### 10.1 가장 먼저 읽기

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`
3. 이 문서

### 10.2 첫 작업 순서

1. `features/signup` 폴더 구조부터 만든다.
2. `types/signup.ts`에서 타입을 먼저 정의한다.
3. `api/signup.ts`를 만든다.
4. `model/signup-store.ts`, `model/use-signup-flow.ts`를 만든다.
5. 그 다음 `pages/signup-start`부터 화면을 순서대로 만든다.

### 10.3 내일 작업 시 꼭 기억할 점

- `name` 직접 입력을 확정하지 않는다.
- PortOne 인증 결과값으로 받을 수 있게 구조를 유연하게 둔다.
- 회원가입 성공 후 자동 로그인 흐름을 기준으로 설계한다.
- PIN 간편 진입은 이번 작업에서 바로 다 하지 않아도 된다.
- 같은 내용 충돌 시 `FRONTEND_GUIDE.md`를 먼저 따른다.

## 11. 아직 남은 확인 사항

내일 구현 전에 또는 구현하면서 필요할 수 있는 확인 항목:

- PortOne 연동 시 실제 프론트 진입 방식
- PortOne SDK callback 성공/실패 응답 예시 상세
- 인증 완료 후 프론트가 어떤 값을 받을 수 있는지
- 아이디 중복 확인을 별도 API로 할지, 회원가입 제출 시 처리할지
- 자동 로그인 실패 시 사용자에게 보여줄 메시지 정책
- 회원가입 API의 실패 응답 예시 상세

## 12. 한 줄 요약

내일은 `features/signup`의 타입, API, 상태 구조를 먼저 만들고, 회원가입을 PortOne 대응형 구조로 구현 시작하면 된다.
