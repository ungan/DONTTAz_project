export interface AccountOpenApiResponse<T> {
  status: string;
  message: string;
  data: T;
}

export interface DemandDepositProduct {
  accountTypeUniqueNo: string;
  bankCode: string;
  bankName: string;
  accountTypeCode: string;
  accountTypeName: string;
  accountName: string;
  accountDescription: string;
  accountType: string;
}

export interface DemandDepositProductListData {
  products: DemandDepositProduct[];
}

export interface CreateAccountRequest {
  accountTypeUniqueNo: string;
}

export interface CreateAccountResponseData {
  bankCode: string;
  accountNo: string;
  currency: string;
  currencyName: string;
}

export interface CurrentUserSummaryResponseData {
  userId: number;
  email: string;
  name: string;
  phoneNumber?: string | null;
}
