# account_transaction 정리

## 목적

거래성 API에서 저장하는 로컬 거래내역 모델을 외부 뱅킹 API 기준에 맞춰 정리한다.

기존 `Transaction` 엔티티는 로컬 표시용 필드 중심이었고, 출처별 거래 고유번호와 원본 거래 타입 정보를 충분히 보존하지 못했다.
이번 정리에서는 `account_transaction` 테이블을 기준으로 다음 정보를 유지한다.

- 출처 시스템 기준 거래 고유번호
- 출처 구분
- 원본 거래 타입 코드와 이름
- 거래 시각
- 금액, 거래 후 잔액
- 상대 계좌 정보와 거래 요약
- 동기화 시각과 저장 출처

## 현재 스키마 기준

엔티티는 `src/main/java/com/donttaz/donttazapi/account/domain/Transaction.java` 기준으로 아래 구조를 가진다.

- 테이블명: `account_transaction`
- PK: `account_tx_id`
- 유니크 키: `(account_id, source, source_tx_id)`
- 인덱스:
  - `(account_id, transaction_at)`
  - `(user_id, transaction_at)`
  - `(account_id, tx_type, transaction_at)`

주요 컬럼:

- `account_tx_id`
- `account_id`
- `user_id`
- `source`
- `source_tx_id`
- `tx_type`
- `tx_type_name`
- `amount`
- `balance_snapshot`
- `counterparty_name`
- `ai_category`
- `counterparty_bank`
- `counterparty_account_no`
- `transaction_summary`
- `transaction_at`
- `synced_at`
- `created_at`
- `updated_at`

컬럼 의미:

- `source`: 거래를 제공한 출처 시스템 구분값이다. 예: `FIN_API`, `VAULT`
- `source_tx_id`: 출처 시스템이 발급한 거래 고유번호다.
- `tx_type`: 출처 시스템의 원본 거래 타입 코드다. 현재 finAPI 기준으로 `transactionType` 값을 그대로 저장한다.
- `tx_type_name`: 출처 시스템의 원본 거래 타입 이름이다. 현재 finAPI 기준으로 `transactionTypeName` 값을 그대로 저장한다.
- `balance_snapshot`: 거래 직후 잔액 스냅샷이다.

## 모델링 기준

- 금액 관련 필드는 `Integer` 대신 `Long`을 사용한다.
- 거래 중복 방지는 `source + source_tx_id` 조합으로 처리한다.
- 외부 API 원본 복원성을 위해 거래 타입 코드와 이름을 함께 저장한다.
- `user_id`는 조회와 집계 편의를 위한 소유자 스냅샷 컬럼이다.
- 표시용 가공 필드보다 외부 원본 의미를 먼저 보존한다.

## 운영 반영 주의사항

운영 프로필은 `src/main/resources/application-prod.properties`에서 `spring.jpa.hibernate.ddl-auto=none`이다.

따라서 아래 변경은 배포 전에 수동 반영이 필요하다.

- 테이블명 `account_transaction` 생성 또는 기존 거래 테이블 마이그레이션
- PK, 유니크 키, 인덱스 생성
- 금액 컬럼 타입 확장
- 출처별 거래 고유번호 및 원본 타입 컬럼 추가
- `user_id`, `ai_category`, `counterparty_bank` 컬럼 추가

`application-local.properties`의 `ddl-auto=update`만으로는 운영 스키마가 맞춰지지 않는다.
기존 로컬 DB에 `account_transaction_id` 같은 이전 PK 컬럼이 이미 있으면, `account_tx_id`로의 변경은 자동 rename되지 않고 add column으로 시도되어 부팅 시점에 실패할 수 있다.
