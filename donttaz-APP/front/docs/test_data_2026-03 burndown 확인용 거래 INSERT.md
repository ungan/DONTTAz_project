USE test_khi;

INSERT INTO account_transaction (
  user_id,
  account_id,
  amount,
  balance_snapshot,
  counterparty_name,
  ai_category,
  transaction_summary,
  transaction_at,
  tx_type,
  tx_type_name,
  source,
  source_tx_id,
  synced_at,
  created_at,
  updated_at,
  is_deleted
) VALUES
(32, 2, 13000, 2093500, '편의점', '식비', '식비(버다운 테스트)', '2026-03-02 08:20:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260302-001', NOW(), NOW(), NOW(), 0),
(32, 2, 9000, 2084500, '버스', '교통', '교통(버다운 테스트)', '2026-03-04 18:10:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260304-001', NOW(), NOW(), NOW(), 0),
(32, 2, 25000, 2059500, '다이소', '생활', '생활(버다운 테스트)', '2026-03-07 16:40:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260307-001', NOW(), NOW(), NOW(), 0),
(32, 2, 17000, 2042500, '스타벅스', '식비', '식비(버다운 테스트)', '2026-03-10 14:00:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260310-001', NOW(), NOW(), NOW(), 0),
(32, 2, 48000, 1994500, '무신사', '쇼핑', '쇼핑(버다운 테스트)', '2026-03-13 20:30:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260313-001', NOW(), NOW(), NOW(), 0),
(32, 2, 12000, 1982500, '지하철', '교통', '교통(버다운 테스트)', '2026-03-16 08:50:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260316-001', NOW(), NOW(), NOW(), 0),
(32, 2, 39000, 1943500, 'CGV', '여가', '여가(버다운 테스트)', '2026-03-19 21:10:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260319-001', NOW(), NOW(), NOW(), 0),
(32, 2, 22000, 1921500, '올리브영', '생활', '생활(버다운 테스트)', '2026-03-22 19:25:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260322-001', NOW(), NOW(), NOW(), 0),
(32, 2, 15000, 1906500, '김밥천국', '식비', '식비(버다운 테스트)', '2026-03-24 12:15:00', 'WITHDRAWAL', '출금', 'FIN_API', 'BD-20260324-001', NOW(), NOW(), NOW(), 0);

INSERT INTO transaction_ai_metadata (
  account_tx_id,
  ai_category,
  confidence_score,
  is_modified,
  modified_category,
  modified_at,
  created_at,
  updated_at,
  is_deleted
)
SELECT
  t.account_tx_id,
  t.ai_category,
  0.99,
  0,
  NULL,
  NULL,
  NOW(),
  NOW(),
  0
FROM account_transaction t
WHERE t.source_tx_id IN (
  'BD-20260302-001',
  'BD-20260304-001',
  'BD-20260307-001',
  'BD-20260310-001',
  'BD-20260313-001',
  'BD-20260316-001',
  'BD-20260319-001',
  'BD-20260322-001',
  'BD-20260324-001'
);
