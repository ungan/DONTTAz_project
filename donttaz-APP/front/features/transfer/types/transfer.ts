export const BANK_CODE_MAP = {
  한국은행: '001',
  산업은행: '002',
  기업은행: '003',
  국민은행: '004',
  농협은행: '011',
  우리은행: '020',
  'SC제일은행': '023',
  시티은행: '027',
  대구은행: '032',
  광주은행: '034',
  제주은행: '035',
  전북은행: '037',
  경남은행: '039',
  새마을금고: '045',
  'KEB하나은행': '081',
  신한은행: '088',
  카카오뱅크: '090',
  싸피은행: '999',
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
