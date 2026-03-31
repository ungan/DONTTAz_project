# 계좌 개설 설계안 2026-03-19

## 1. 기준 문서

- 1순위: `donttaz-APP/FRONTEND_GUIDE.md`
- 보완 기준: `donttaz-APP/front/docs/design-guide.md`

이번 설계는 `FRONTEND_GUIDE`의 레이어 규칙을 먼저 따르고, 구체적인 여백/버튼/입력/스타일 분리 방식은 `design-guide` 기준으로 맞춘다.

---

## 2. 화면 파악

이미지 기준 계좌 개설 플로우는 5단계로 보인다.

1. 약관 동의
2. 고객정보 입력
3. 전자금융 신청
4. 계좌 인증
5. 개설 완료

각 단계의 목적은 아래와 같다.

### 2.1 약관 동의

- 필수 약관과 선택 약관을 구분해서 보여준다.
- `필수 약관 전체 동의`, `선택 약관 전체 동의`, 개별 약관 체크가 필요하다.
- 다음 버튼은 필수 약관이 모두 동의되기 전까지 비활성화한다.

### 2.2 고객정보 입력

- 이름, 영문명, 휴대전화, 주소, 이메일 입력
- SMS / 알림톡 / 우편물 수신 동의 같은 선택값 포함 가능
- 주소는 우편번호 찾기 버튼과 연결될 여지가 있다.

### 2.3 전자금융 신청

- 뱅킹 ID
- 뱅킹 비밀번호
- 비밀번호 확인
- 1일 이체 한도 선택

### 2.4 계좌 인증

- 타행 입금자명에 포함된 숫자 4자리 입력
- 숫자 입력 UI가 필요하다.
- 기존 PIN UI와 비슷하지만 완전히 같은 컴포넌트는 아닐 수 있다.

### 2.5 개설 완료

- 계좌 개설 완료 메시지
- 상품명
- 신규 계좌번호
- `추가 연결`, `확인` 버튼

---

## 3. 레이어 설계

`FRONTEND_GUIDE` 기준으로 아래 구조가 가장 자연스럽다.

### 3.1 app

라우트만 둔다.

- `app/account-open-terms.tsx`
- `app/account-open-profile.tsx`
- `app/account-open-banking.tsx`
- `app/account-open-verify.tsx`
- `app/account-open-complete.tsx`

각 파일은 해당 `pages` export만 한다.

### 3.2 pages

화면 단위 조합만 담당한다.

- `pages/account-open-terms`
- `pages/account-open-profile`
- `pages/account-open-banking`
- `pages/account-open-verify`
- `pages/account-open-complete`

각 page는 `PageHeader`, 버튼, feature 섹션을 조합해서 하나의 화면을 만든다.

### 3.3 features

실제 계좌 개설 행동 단위는 `features/account-open`로 묶는다.

예상 구성:

- `features/account-open/types/account-open.ts`
- `features/account-open/model/account-open-store.ts`
- `features/account-open/model/use-account-open-flow.ts`
- `features/account-open/api/account-open.ts`
- `features/account-open/ui/*`

여기에는 단계 진행, 입력 검증, 약관 체크, API 호출, 완료 상태 정리가 들어간다.

### 3.4 entities

개설 완료 후 결과 계좌를 기존 계좌 상태와 연결할 때만 `entities/bank-account`를 쓴다.

즉:

- 개설 중인 임시 상태 = `features/account-open`
- 개설 완료 후 앱 전체에서 쓸 계좌 상태 = `entities/bank-account`

---

## 4. 공용 컴포넌트 재사용 기준

`design-guide.md` 기준으로 먼저 existing shared UI를 확인하고 재사용한다.

### 4.1 바로 재사용할 가능성이 높은 것

- `shared/ui/button.tsx`
- `shared/ui/custom-input.tsx`
- `shared/ui/page-header.tsx`
- `shared/ui/text-link.tsx`
- `shared/ui/confirm-modal.tsx`

### 4.2 상황 보고 재사용/확장할 것

- `shared/ui/numeric-keypad.tsx`
- `shared/ui/pin-indicator.tsx`

계좌 인증의 4자리 숫자 입력은 PIN과 비슷하지만, 인증 문구/길이/레이아웃이 다를 수 있으니 그대로 쓰기보다 확장 가능한지 먼저 본다.

### 4.3 새로 만들 가능성이 높은 UI

- 약관 아코디언 리스트
- 약관 동의 행
- 이체한도 선택칩
- 계좌 개설 완료 요약 카드

이것들은 `shared`보다 먼저 `features/account-open/ui`에 두는 쪽이 안전하다. 다른 곳에서도 재사용 필요가 생기면 그때 shared로 올린다.

---

## 5. 상태 설계

`signup-store`, `transfer-store` 패턴을 따라 Zustand store 하나로 묶는 게 좋다.

### 5.1 store에 들어갈 값

- `currentStep`
- `errorMessage`
- `isSubmitting`
- `isFetchingTerms`
- `isCreatingAccount`
- `isVerifyingAccount`
- `requiredTerms`
- `optionalTerms`
- `customerName`
- `englishName`
- `phoneNumber`
- `zipCode`
- `address`
- `addressDetail`
- `emailLocal`
- `emailDomain`
- `isSmsAgreed`
- `isAlimtalkAgreed`
- `isMailAgreed`
- `bankingId`
- `bankingPassword`
- `bankingPasswordConfirm`
- `dailyTransferLimit`
- `verificationCode`
- `createdAccountNumber`
- `createdProductName`

### 5.2 store 액션

- 약관 전체 동의
- 약관 개별 토글
- 입력 필드 업데이트
- 단계 이동
- 로딩값 업데이트
- 완료 데이터 세팅
- 전체 초기화

---

## 6. API 설계 방향

API는 `entities`보다 `features/account-open/api`에 두는 게 맞다.

이유:

- 지금 관심사는 계좌 조회가 아니라 `계좌 개설`이라는 사용자 행동이기 때문
- `FRONTEND_GUIDE`의 entities vs features 기준에도 맞다

예상 API 함수:

- `getAccountOpenTermsApi()`
- `submitAccountOpenProfileApi()`
- `submitElectronicBankingApi()`
- `verifyAccountOpenApi()`
- `createAccountApi()`

백엔드가 아직 단계별 API가 안 나와 있으면, 우선 프론트에서는 step별 mock 흐름으로 설계하고 마지막에 실제 API를 연결한다.

---

## 7. 스타일 기준

`design-guide.md`에서 읽힌 핵심 기준을 그대로 따른다.

- 기본 수평 여백은 `16`
- 간격은 `8`, `16`, `48` 중심
- 입력과 버튼 높이는 기존처럼 `52`
- 공용 입력/버튼 radius는 `8`
- 배경은 흰색 중심
- 포인트 컬러는 `Primary`
- 스타일은 JSX에 직접 넣지 말고 `*.styles.ts`로 분리

즉 계좌 개설 플로우는 새 디자인 실험보다, 지금 로그인/회원가입/이체와 같은 톤으로 붙는 것이 맞다.

---

## 8. 구현 순서 제안

### 1단계: 플로우 뼈대

- 라우트 5개 생성
- pages 5개 생성
- 이동만 되는 최소 골격 구현

### 2단계: 상태와 검증

- `account-open-store.ts`
- `use-account-open-flow.ts`
- 단계별 유효성 검증

### 3단계: 약관 화면

- 필수/선택 약관 목록
- 전체 동의
- 다음 버튼 활성화 조건

### 4단계: 고객정보 화면

- `CustomInput` 재사용
- 주소/이메일 조합 구조 설계

### 5단계: 전자금융 화면

- ID, 비밀번호, 확인
- 이체한도 선택

### 6단계: 계좌 인증 화면

- 4자리 숫자 입력
- 인증 완료 버튼

### 7단계: 완료 화면

- 개설 결과 표시
- 홈 또는 다음 액션 연결

### 8단계: API 연결

- mock 제거
- 실제 백엔드 스펙 반영
- 완료 후 `bank-account-store` 반영 검토

---

## 9. 내일 바로 시작할 추천 범위

내일은 약관 단계부터 시작하는 것이 제일 좋다.

이유:

- 첫 화면이라 전체 플로우 톤을 잡기 좋다.
- 체크 상태와 비활성화 로직이 계좌 개설의 핵심 구조가 된다.
- API가 아직 불완전해도 mock으로 먼저 만들기 쉽다.

즉 내일 첫 작업 추천 순서는 아래와 같다.

1. `features/account-open` 폴더 생성
2. `account-open-store.ts` 생성
3. `account-open-terms` route/page 생성
4. 약관 리스트 MVP 구현
5. 필수 약관 완료 시 다음 단계 이동

---

## 10. 확인이 필요한 백엔드/기획 항목

아래는 구현 전에 확인되면 좋다.

- 약관 목록을 서버에서 내려주는지, 프론트 고정값인지
- 고객정보 입력 항목이 최종 확정인지
- 영문명 필수 여부
- 주소 검색 방식
- 이메일 조합 규칙
- 뱅킹 ID 중복 확인 필요 여부
- 전자금융 비밀번호 규칙
- 1일 이체한도 값 목록이 고정인지
- 1원 인증 숫자 자리 수가 4자리로 고정인지
- 완료 후 `추가 연결` 버튼의 실제 목적

---

## 11. 결론

계좌 개설은 `signup`처럼 다단계 플로우로 보고, `features/account-open` 중심으로 설계하는 게 맞다.

핵심 원칙은 아래 두 줄로 정리된다.

- 구조는 `FRONTEND_GUIDE` 기준: `app -> pages -> features -> entities/shared`
- 스타일은 `design-guide` 기준: 공용 컴포넌트 우선 재사용, 여백/버튼/입력 규칙 통일

다음 구현 시작점은 `약관 동의 화면 MVP`가 가장 적절하다.
