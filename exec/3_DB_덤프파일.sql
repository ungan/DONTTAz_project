-- 본 더미 덤프 스크립트는 MySQL 로컬 구동환경 이외의 포팅 과정에서 참고용으로 작성된 스키마 예시입니다.
-- Donttaz 서버 사이드 애플리케이션(Spring Boot)은 'spring.jpa.hibernate.ddl-auto=update' 환경 설정을 통해 구동 시 DB 스키마가 자동 생성 및 관리됩니다.
-- 따라서 본 3_DB_덤프파일.sql 을 직접 Import 하지 않아도 서버를 실행하면 자동으로 테이블이 생성됩니다.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for `User`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `kakao_id` varchar(255) NOT NULL UNIQUE,
  `nickname` varchar(50) DEFAULT NULL,
  `main_account_no` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for `Vault` 
-- (논리적 분리 모계좌-사용자 금고 테이블)
-- ----------------------------
DROP TABLE IF EXISTS `vault`;
CREATE TABLE `vault` (
  `vault_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `balance` bigint(20) NOT NULL DEFAULT '0',
  `target_amount` bigint(20) DEFAULT NULL,
  `vault_type` varchar(20) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`vault_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for `Transaction_History`
-- ----------------------------
DROP TABLE IF EXISTS `transaction_history`;
CREATE TABLE `transaction_history` (
  `transaction_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `vault_id` bigint(20) DEFAULT NULL,
  `amount` bigint(20) NOT NULL,
  `type` varchar(20) NOT NULL COMMENT 'DEPOSIT, WITHDRAWAL 등',
  `fintech_tran_id` varchar(100) DEFAULT NULL COMMENT '금융망 API 처리 식별자',
  `created_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`transaction_id`),
  FOREIGN KEY (`vault_id`) REFERENCES `vault`(`vault_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
