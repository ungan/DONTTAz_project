export const BANK_CODE_MAP = {
  국민은행: '004',
  신한은행: '088',
  하나은행: '081',
  우리은행: '020',
  농협은행: '012',
  IBK기업은행: '003',
  SC제일은행: '023',
  씨티은행: '027',
  카카오뱅크: '090',
  토스뱅크: '092',
} as const;

export const BANK_OPTIONS = Object.keys(BANK_CODE_MAP) as BankName[];

export type BankName = keyof typeof BANK_CODE_MAP;

export interface TransferFormValues {
  bankName: BankName | '';
  accountNumber: string;
}

export interface TransferRequest {
  depositAccountNo: string;
  depositTransactionSummary: string;
  transactionBalance: string;
  withdrawalAccountNo: string;
  withdrawalTransactionSummary: string;
}

export interface TransferTransaction {
  transactionUniqueNo: string;
  accountNo: string;
  transactionDate: string;
  transactionType: string;
  transactionTypeName: string;
  transactionAccountNo: string;
}

export interface TransferResult {
  transactions: TransferTransaction[];
}

export interface TransferResponse {
  status: string;
  message: string;
  data: TransferResult;
}
