# Transfer Withdrawal Account Follow-up

## 배경

- 이체 시작 화면에서 `내 출금 계좌 선택` 기능을 프론트에 추가했다.
- 하지만 실제 서버 모드에서 계좌 목록 응답의 계좌번호가 마스킹(`***`)되어 내려와, 프론트가 실제 `withdrawalAccountNo` 원본 값을 확정할 수 없는 상태다.
- 이 상태에서 잘못된 fallback 값을 보내면 송금이 실패하므로, 현재 프론트는 선택 불가 처리로 안전하게 막아둔 상태다.

## 현재 확인된 문제

- 백엔드 계좌 목록 응답에서 계좌번호가 마스킹되어 내려온다.
- 프론트는 송금 요청 시 실제 `withdrawalAccountNo`가 필요하다.
- 원본 계좌번호를 확정할 수 없어서 다음 문구가 노출된다.
  - `이 계좌는 현재 앱에서 출금 계좌번호를 확인할 수 없어 선택할 수 없습니다.`

## 왜 지금은 여기서 멈췄는가

- 프론트에서 억지로 진행하면 예전 mock 계좌번호나 오래된 store 값이 다시 사용될 수 있다.
- 실제로 이전 로그에서 `withdrawalAccountNo: 110123456789` 같은 잘못된 값이 전송되어 `A1003 계좌번호가 유효하지 않습니다.`가 발생했다.
- 따라서 백엔드 응답이 보완되기 전까지는 출금 계좌 선택을 제한하는 현재 동작이 더 안전하다.

## 백엔드 수정 요청 포인트

- 계좌 목록 응답에서 송금에 사용할 수 있는 실제 출금 계좌번호를 프론트가 식별할 수 있어야 한다.
- 아래 중 하나는 필요하다.

### 권장안

- `GET /accounts` 응답에 실제 송금용 계좌번호 필드를 추가
  - 예: `accountNumber`, `rawAccountNo`, `transferAccountNo`

### 함께 있으면 좋은 값

- `accountId`
- `bankName`
- `accountName`
- `accountNo`(마스킹 표시용)
- `accountBalance`
- `isPrimary`

## 프론트 후속 작업

- 백엔드가 실제 계좌번호 필드를 내려주면, `entities/account` 계층에서 해당 필드를 읽어오도록 반영
- `transfer-account` 화면에서 현재 선택 불가 처리된 계좌를 정상 선택 가능 상태로 변경
- `use-transfer-submit`에서 fallback 계좌번호 의존을 제거하고, 사용자가 선택한 출금 계좌만 사용하도록 고정

## 관련 파일

- `front/pages/transfer-account/ui/transfer-account-page.tsx`
- `front/features/transfer/model/transfer-store.ts`
- `front/features/transfer/model/use-transfer-submit.ts`
- `front/entities/account/*`

## 메모

- 이 이슈는 프론트 버그라기보다, 현재 백엔드 응답에서 출금용 원본 계좌번호가 마스킹되어 전달되는 구조 때문에 발생한다.
- 백엔드 수정 전까지는 현재 방어 로직을 유지한다.
