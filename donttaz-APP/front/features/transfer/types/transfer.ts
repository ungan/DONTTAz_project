export const BANK_CODE_MAP = {
  '국민은행':    '004',
  '신한은행':    '088',
  '하나은행':    '081',
  '우리은행':    '020',
  '농협은행':    '012',
  'IBK기업은행': '003',
  'SC제일은행':  '023',
  '씨티은행':    '027',
  '카카오뱅크':  '090',
  '토스뱅크':    '092',
} as const;

export const BANK_OPTIONS = Object.keys(BANK_CODE_MAP) as BankName[];

export type BankName = keyof typeof BANK_CODE_MAP;

export interface TransferFormValues {
  bankName: BankName | '';
  accountNumber: string;
}

export interface TransferRequest {
  withdrawalAccountId: number;
  depositBankCode: string;
  depositAccountNumber: string;
  amount: number;
  withdrawalMemo: string;
  depositMemo: string;
  pinToken: string;
}

export interface TransferResult {
  transactionId: number;
  depositAccountName: string;
  depositBankName: string;
  amount: number;
  fee: number;
  withdrawalBalance: number;
}

export interface TransferResponse {
  status: string;
  message: string;
  data: TransferResult;
}
