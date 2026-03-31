export interface AccountSummary {
  accountId?: number | null;
  bankCode: string;
  bankName: string;
  userName: string;
  accountNo: string;
  accountNumber?: string | null;
  accountName: string;
  accountTypeCode: string;
  accountTypeName: string;
  accountCreatedDate: string;
  accountExpiryDate: string;
  dailyTransferLimit: string;
  oneTimeTransferLimit: string;
  accountBalance: string;
  lastTransactionDate: string;
  currency: string;
  isPrimary: boolean;
}

export interface GetMyAccountsResponseData {
  totalBalance: number;
  accountList: AccountSummary[];
}

export interface GetMyAccountsResponse {
  status: string;
  message: string;
  data: GetMyAccountsResponseData;
}
