# PortOne Device Test Checklist - 2026-03-18

## 1. 목적

이 문서는 회원가입 `signup-start` 단계에서 PortOne 본인인증 흐름을 실기기에서 점검할 때
빠르게 확인해야 할 항목을 정리한 체크리스트다.

현재 프론트 구현 방식은 아래 흐름을 기준으로 한다.

1. 앱에서 `본인인증 시작` 클릭
2. 브라우저 인증 세션으로 백엔드의 `test-auth.html` 오픈
3. PortOne SDK 본인인증 진행
4. 성공 시 `identityVerificationId`를 앱 딥링크로 전달
5. 프론트에서 `/auth/verify-identity` 호출
6. 성공 시 `signup-account` 화면 이동

## 2. 사전 준비

### 2.1 프론트 `.env`

`front/.env` 확인:

```env
EXPO_PUBLIC_IS_MOCK=false
EXPO_PUBLIC_BASE_URL=http://PC_IP:8080/api/v1
```

주의:

- `localhost`가 아니라 실기기에서 접근 가능한 PC의 로컬 IP를 사용한다.
- `/api/v1`를 반드시 포함한다.

### 2.2 백엔드 접근 확인

실기기 브라우저 또는 같은 와이파이 환경에서 아래 주소가 열리는지 확인한다.

```txt
http://PC_IP:8080/test-auth.html
```

열리지 않으면:

- 백엔드 서버 실행 여부 확인
- PC 방화벽 확인
- 휴대폰과 PC가 같은 네트워크인지 확인

### 2.3 앱 딥링크 기준

현재 앱 `scheme`은 `front`다.

예상 callback 형태:

```txt
front://signup-start?identityVerificationId=...
```

## 3. 테스트 순서

### 3.1 앱 진입

1. Expo 앱 실행
2. 로그인 화면에서 `이메일로 회원가입` 클릭
3. `signup-start` 화면 진입 확인

정상 기대 결과:

- 회원가입 시작 화면이 보인다.
- `본인인증 시작` 버튼이 보인다.

### 3.2 인증 세션 진입

1. `본인인증 시작` 버튼 클릭
2. 브라우저 인증 세션이 열리는지 확인

정상 기대 결과:

- 백엔드의 `test-auth.html` 화면이 열린다.
- PortOne 인증 버튼이 보인다.

비정상 포인트:

- 브라우저가 열리지 않음
- `test-auth.html` 접근 실패
- 백엔드 주소 연결 실패

### 3.3 PortOne 인증

1. 테스트 페이지에서 `본인인증 시작` 클릭
2. 인증 완료

정상 기대 결과:

- PortOne SDK 응답에서 `identityVerificationId`가 생성된다.
- 테스트 페이지가 앱 딥링크로 복귀한다.

비정상 포인트:

- 인증 실패 시 alert 메시지만 뜨고 앱으로 복귀하지 않음
- 브라우저에서 복귀는 했지만 앱에서 다음 화면으로 가지 않음

### 3.4 프론트 복귀 후 결과 조회

1. 앱으로 복귀했는지 확인
2. 자동으로 `/auth/verify-identity`가 호출되는지 확인

정상 기대 결과:

- 인증 성공 시 `signup-account` 화면으로 이동
- 인증 결과 이름/휴대폰 번호가 요약 영역에 표시됨

비정상 포인트:

- `본인인증이 취소되었거나 완료되지 않았습니다.` 메시지
- `본인인증 결과를 확인할 수 없습니다.` 메시지
- 백엔드 `400`, `409`, `500` 응답

## 4. 확인해야 할 로그/상태

### 4.1 프론트

- `signup-start` 버튼 클릭 직후 로딩 표시
- 앱 복귀 후 에러 메시지 표시 여부
- `signup-account`로 정상 라우팅되는지

### 4.2 백엔드

- `test-auth.html` 접근 로그
- `/api/v1/auth/verify-identity` 요청 로그
- PortOne API 조회 성공/실패 로그

## 5. 자주 막히는 지점

### 5.1 `/api/v1` 누락

증상:

- 회원가입/인증 조회 API가 404

확인:

- `EXPO_PUBLIC_BASE_URL`이 `http://PC_IP:8080/api/v1`인지 확인

### 5.2 실기기에서 백엔드 페이지 접근 불가

증상:

- 인증 세션은 열리지만 `test-auth.html` 자체가 안 열림

확인:

- PC IP 사용 여부
- 같은 와이파이 여부
- 방화벽 허용 여부

### 5.3 딥링크 복귀 실패

증상:

- 인증은 끝났는데 앱으로 안 돌아옴

확인:

- 앱 `scheme=front` 설정 여부
- redirectUri가 `front://signup-start...` 형태인지
- 브라우저가 `window.location.href`로 딥링크 이동 가능한지

### 5.4 PortOne 응답 파라미터 불일치

증상:

- 복귀는 했지만 `identityVerificationId`를 못 읽음

확인:

- callback URL query param 이름이 정확히 `identityVerificationId`인지
- 실패 시 `code`, `message`가 query param으로 들어오는지

## 6. 테스트 후 바로 기록할 항목

테스트가 끝나면 아래를 바로 남긴다.

- 성공 여부
- 사용한 휴대폰 기종
- 사용한 `.env`의 PC IP
- 브라우저 진입 성공 여부
- 앱 복귀 성공 여부
- `signup-account` 이동 성공 여부
- 실패 시 에러 메시지 원문
- 백엔드 로그에 남은 상태 코드

## 7. 한 줄 정리

실기기 테스트의 핵심은 `test-auth.html 접근 가능`, `PortOne 완료 후 front:// 딥링크 복귀`, `identityVerificationId로 verify-identity 호출 성공` 이 세 가지다.
