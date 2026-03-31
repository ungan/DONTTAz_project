# Account Open Identity Plan - 2026-03-20

## 1. 배경

현재 계좌 개설 플로우는 아래 5단계로 구성되어 있다.

1. 약관 동의
2. 고객정보 입력
3. 전자금융 신청
4. 인증 단계
5. 완료

기존에는 4단계를 `1원 인증`으로 보고 화면만 mock으로 두었지만, 실제 구현 상태와 백엔드 구조를 같이 보면 당장은 `기존 회원가입 본인인증 재사용` 방향이 더 현실적이다.

## 2. 왜 방향을 바꾸는가

### 2.1 현재 1원 인증 상태

- 프론트에는 숫자 4자리 입력 UI만 있다.
- 실제 1원 인증 요청/확인 API는 아직 프론트에 붙어 있지 않다.
- 백엔드에도 관련 도메인/DTO 흔적은 있으나, 지금 계좌 생성 API와 직접 연결된 구현은 확인되지 않았다.

### 2.2 현재 계좌 생성 API 구조

실제 `POST /api/v1/accounts`는 현재 아래 요청만 받는다.

```json
{
  "accountTypeUniqueNo": "..."
}
```

즉 지금 구현 기준 계좌 생성 자체는 `1원 인증 성공 결과`보다 `상품 선택(accountTypeUniqueNo)`에 더 직접적으로 연결되어 있다.

### 2.3 이미 붙어 있는 본인인증 흐름

회원가입에서 PortOne 본인인증 흐름을 이미 한 번 붙여서 아래가 가능하다.

- PortOne 본인인증 실행
- callback으로 `identityVerificationId` 수신
- `/auth/verify-identity`로 이름 / 휴대폰번호 조회

따라서 계좌 개설 단계에서도 같은 흐름을 재사용할 수 있다.

## 3. 목표

계좌 개설의 인증 단계를 아래처럼 바꾼다.

1. 계좌 개설 verify 단계 진입
2. 기존 PortOne 본인인증 실행
3. 인증 결과의 이름 / 휴대폰번호 확보
4. 현재 로그인 사용자 정보와 일치 여부 확인
5. 일치하면 `POST /api/v1/accounts` 호출
6. 완료 화면으로 이동

## 4. 현재 확인된 구조

### 4.1 프론트에서 재사용 가능한 것

- `features/signup/api/portone.ts`
- `requestPortOneIdentityVerification()`
- `parsePortOneCallbackUrl()`
- `verifyIdentityApi()`

### 4.2 백엔드에서 현재 가능한 것

- `/auth/verify-identity`로 이름 / 휴대폰번호 확인 가능
- `/users/me`로 로그인 사용자 정보 조회 가능

### 4.3 현재 백엔드의 부족한 점

`/users/me` 응답에는 현재 아래만 있다.

- `userId`
- `email`
- `name`

즉 **휴대폰번호가 없다.**

그래서 현재 상태로는:

- 이름 비교는 가능
- 휴대폰번호 비교는 불가능

## 5. 선택지

### 선택지 A. 프론트 임시 진행

계좌 개설 verify 단계에서:

- PortOne 본인인증 재사용
- `/auth/verify-identity` 호출
- `/users/me` 호출
- 이름만 우선 비교
- 휴대폰번호 비교는 TODO로 남김

장점:

- 프론트만으로 바로 진행 가능
- 인증 단계의 UX는 바로 바뀜

단점:

- 사용자가 원한 "이름 + 휴대폰번호" 이중 확인이 완전하지 않음

### 선택지 B. 정식 진행

백엔드에서 아래 둘 중 하나를 먼저 보완한다.

1. `/users/me` 응답에 `phoneNumber` 추가
2. 별도 비교 API 추가

예:

- `POST /api/v1/accounts/verify-owner`
- body: `identityVerificationId`
- 서버가 현재 로그인 사용자와 본인인증 결과를 직접 비교

장점:

- 금융 서비스 관점에서 더 안전
- 프론트가 민감한 비교 로직을 덜 가짐

단점:

- 백엔드 수정이 필요

## 6. 추천

추천은 아래 순서다.

1. **정식 방향은 선택지 B**
2. 하지만 프론트 작업을 먼저 이어가야 하면 **선택지 A로 중간 단계 구현**

즉 현실적인 진행 순서는:

1. 계좌 개설 verify 화면을 PortOne 본인인증 재사용 구조로 변경
2. 이름 비교까지 먼저 붙이기
3. 휴대폰번호 비교는 백엔드 응답 확장 후 마무리

## 7. 구현 계획

### 7.1 1차 프론트 리팩터링

- `account-open-verify` 화면에서 숫자 4자리 UI 제거
- "본인인증 다시 하기" 버튼 중심 구조로 변경
- 기존 signup PortOne 함수 재사용

### 7.2 계좌 개설 전 인증 성공 처리

- `identityVerificationId` 수신
- `/auth/verify-identity` 호출
- 이름 / 휴대폰번호 확보

### 7.3 현재 로그인 사용자 정보 조회

- `/users/me` 호출
- 현재 이름 확인
- 현재 응답 기준으로는 이름만 비교 가능

### 7.4 비교 성공 시 계좌 생성

- 이미 store에 저장된 `accountTypeUniqueNo` 사용
- `POST /api/v1/accounts` 호출
- 성공 응답을 완료 화면에 전달

### 7.5 비교 실패 시 처리

- 이름 불일치
- 휴대폰번호 불일치
- 인증 취소
- 인증 실패

각 경우에 맞는 에러 문구 분리

## 8. 백엔드에 확인 / 요청할 것

### 8.1 우선 확인

- `/users/me`에 `phoneNumber`를 넣을 수 있는지

### 8.2 가능하면 더 좋은 방향

서버가 직접 비교하는 API 추가

예시:

```txt
POST /api/v1/accounts/verify-owner
body: { "identityVerificationId": "..." }
```

응답 예시:

```json
{
  "status": "SUCCESS",
  "message": "본인 확인 완료",
  "data": {
    "matched": true
  }
}
```

## 9. 현재 결론

- 지금 1원 인증을 그대로 밀기보다 PortOne 본인인증 재사용 방향이 더 적절하다.
- 다만 "이름 + 휴대폰번호 완전 비교"를 하려면 백엔드 응답 보완이 하나 필요하다.
- 프론트만 먼저 진행한다면 verify 단계를 PortOne 재인증 단계로 바꾸고, 이름 비교부터 먼저 붙이는 방식이 가장 현실적이다.
