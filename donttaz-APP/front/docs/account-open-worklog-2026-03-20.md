# Account Open Worklog - 2026-03-20

## 1. 문서 목적

이 문서는 계좌 개설 플로우 관련 프론트 작업 내용을 한 번에 정리하고, 다음 작업자가 바로 이어서 볼 수 있도록 현재 구현 범위, mock 처리 범위, 남은 API 연동 포인트를 남기기 위한 메모다.

설계 기준은 아래 순서를 따랐다.

1. `donttaz-APP/FRONTEND_GUIDE.md`
2. `donttaz-APP/front/docs/design-guide.md`

겹치는 내용은 `FRONTEND_GUIDE.md`를 우선으로 보고, 화면 조합과 공용 UI 사용 방식은 `design-guide.md`를 참고했다.

## 2. 이번 작업 요약

### 2.1 계좌 개설 플로우 골격 추가

- 계좌 개설 흐름을 5단계 화면으로 구성했다.
- 현재 단계는 아래 순서다.
  1. 약관 동의
  2. 고객정보 입력
  3. 전자금융 신청
  4. 1원 인증
  5. 개설 완료

### 2.2 구조 분리

- `app`에는 라우트만 두고
- `pages`에는 화면 조합을 두고
- `features/account-open`에 상태와 검증, 단계 이동 로직을 모았다.

현재 구조 방향은 회원가입 플로우와 비슷하게 맞췄다.

### 2.3 1원 인증은 mock으로 처리

- 1원 인증 단계는 지금 실제 API 연동이 아니라 화면 흐름 확인용 mock 상태다.
- 숫자 4자리를 입력하면 다음 단계로 넘어가게 해두었다.
- 실제 `consentToken` 생성이나 검증 API는 아직 붙이지 않았다.

### 2.4 완료 화면도 mock 결과 표시

- 완료 화면에서는 상품명, 신규 계좌번호, 계좌 별칭을 mock 데이터처럼 보여주도록 구성했다.
- `추가 연결`은 계좌 개설 시작으로 돌아가고
- `확인`은 홈으로 이동하게 했다.

## 3. 구현한 화면 흐름

### 3.1 약관 동의

- 필수/선택 약관 체크 상태 관리
- 필수 약관 미동의 시 다음 버튼 비활성화
- 계좌 개설 첫 진입점 역할

### 3.2 고객정보 입력

- 이름
- 영문명
- 휴대전화번호
- 우편번호/주소/상세주소
- 이메일
- 수신 동의 항목

기본 입력 검증을 넣어 다음 단계로 넘어갈 수 있게 했다.

### 3.3 전자금융 신청

- 뱅킹 ID
- 뱅킹 비밀번호
- 비밀번호 확인
- 1일 이체 한도

이 단계도 현재는 프론트 기준 입력 검증만 들어가 있다.

### 3.4 1원 인증

- 4자리 숫자 입력 UI
- 공용 숫자 키패드 재사용
- 현재는 mock 단계 안내 문구를 함께 표시

### 3.5 개설 완료

- 계좌 개설 완료 메시지
- 상품명 표시
- 신규 계좌번호 표시
- 계좌 별칭 표시
- 후속 버튼 2개 제공

## 4. 주요 수정 파일

### 4.1 설계 문서

- `front/docs/account-open-plan-2026-03-19.md`

### 4.2 라우트

- `front/app/account-open-terms.tsx`
- `front/app/account-open-profile.tsx`
- `front/app/account-open-banking.tsx`
- `front/app/account-open-verify.tsx`
- `front/app/account-open-complete.tsx`
- `front/app/_layout.tsx`

### 4.3 pages

- `front/pages/account-open-terms/ui/account-open-terms-page.tsx`
- `front/pages/account-open-profile/ui/account-open-profile-page.tsx`
- `front/pages/account-open-banking/ui/account-open-banking-page.tsx`
- `front/pages/account-open-verify/ui/account-open-verify-page.tsx`
- `front/pages/account-open-complete/ui/account-open-complete-page.tsx`

### 4.4 features/account-open

- `front/features/account-open/model/account-open-store.ts`
- `front/features/account-open/model/use-account-open-flow.ts`
- `front/features/account-open/types/account-open.ts`
- `front/features/account-open/ui/account-open-progress.tsx`
- `front/features/account-open/ui/account-open-terms-section.tsx`
- `front/features/account-open/ui/account-open-profile-form.tsx`
- `front/features/account-open/ui/account-open-banking-form.tsx`
- `front/features/account-open/ui/account-open-verification-code.tsx`

### 4.5 메뉴 진입

- `front/pages/menu/ui/menu-page.tsx`

메뉴 탭에서 계좌 개설 화면으로 들어갈 수 있게 연결했다.

## 5. 현재 상태 정리

### 5.1 지금 가능한 것

- 메뉴에서 계좌 개설 진입
- 약관 -> 고객정보 -> 전자금융 -> 1원 인증 -> 완료
- 전체 플로우 한 바퀴 확인
- 단계별 입력 검증
- 완료 화면까지 자연스럽게 이동

### 5.2 아직 mock인 부분

- 1원 인증 실제 API
- `consentToken` 생성
- 최종 `POST /api/v1/accounts` 연동
- 실제 오픈 API 계좌 생성 결과 표시

### 5.3 현재 가정

- 백엔드는 내부적으로 오픈 API를 통해 계좌를 생성할 수 있다.
- 프론트의 다단계 입력값은 최종적으로 백엔드에서 조합되거나 중간 단계에서 사용될 수 있다.
- 지금은 UI/흐름 우선으로 만들고, API는 나중에 연결하는 방향으로 진행했다.

## 6. API 관련 현재 해석

현재 받은 문서 기준 최종 생성 API는 아래다.

- `POST /api/v1/accounts`

요청 바디는 현재 문서상 아래 값만 받는다.

- `bankCode`
- `accountNumber`
- `accountAlias`
- `consentToken`

하지만 실제 업무 흐름상 프론트에서 입력한 고객정보/전자금융정보가 내부 오픈 API 호출에 쓰일 가능성이 있다. 그래서 지금 프론트 다단계 화면이 완전히 불필요하다고 보지는 않았다.

## 7. 다음에 백엔드 확인이 필요한 것

아래 항목이 확인되면 실제 API 연동 방향을 확정할 수 있다.

1. 고객정보 입력값은 어느 단계에서 쓰이는지
2. 전자금융 신청의 `뱅킹 ID`, `비밀번호`가 실제 생성 과정에 필요한지
3. `consentToken`은 어떤 API 응답으로 받는지
4. 최종 `POST /accounts` 전에 중간 단계 API가 따로 있는지
5. 1원 인증이 실제로 어떤 값으로 검증되는지

## 8. 다음 작업 추천 순서

### 8.1 API 연동 전

- 현재 mock 플로우를 한번 더 화면 기준으로 점검
- 문구/버튼/입력 UX 보정
- 필요한 공용 컴포넌트 재사용 여부 정리

### 8.2 API 연동 시작 시

1. 1원 인증 요청/확인 API 확인
2. `consentToken` 생성 흐름 연결
3. 최종 `POST /api/v1/accounts` 연결
4. 완료 화면을 실제 응답값으로 교체
5. 생성 완료 후 계좌 목록 스토어와 연결

## 9. 비고

- 현재 1원 인증 단계는 의도적으로 mock으로 남겨두었다.
- 이 단계는 "화면만 먼저 만들고 넘어가게 해달라"는 기준으로 맞춰둔 상태다.
- 나중에 실제 연동 시 verify 단계와 complete 단계만 교체하면 된다.
