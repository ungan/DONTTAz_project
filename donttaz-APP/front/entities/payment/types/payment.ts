export type TransactionType = 'ALL' | 'DEPOSIT' | 'WITHDRAWAL';

export interface Transaction {
  transactionId: number;
  transactionDate: string;
  type: TransactionType;
  amount: number;
  balanceAfter: number;
  counterpartyName: string;
  category: string;
}

export interface GetPaymentsRequest {
  accountId: number;
  startDate?: string;
  endDate?: string;
  type?: TransactionType;
  page?: number;
  size?: number;
}

export interface GetPaymentsResponseData {
  transactions: Transaction[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
  isLast: boolean;
}

export interface GetPaymentsResponse {
  status: string;
  message: string;
  data: GetPaymentsResponseData;
}
