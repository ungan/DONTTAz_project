export interface CategorizedTransaction {
  transactionId: number;
  transactionDate: string;
  storeName: string;
  amount: number;
  aiCategory: string;
  confidenceScore: number;
  isModified: boolean;
}

export interface MonthlyReportResponse {
  status: string;
  message: string;
  data: {
    totalElements: number;
    categorizedList: CategorizedTransaction[];
  };
}

export interface MonthlyReportParams {
  yearMonth: string;
  page?: number;
  size?: number;
}
