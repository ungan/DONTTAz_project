export const ACCOUNT_OPEN_PORTONE_CALLBACK_PATH = '/account-open-verify';

export type AccountOpenStep = 'terms' | 'profile' | 'banking' | 'verify' | 'complete';

export type AccountOpenTermCategory = 'required' | 'optional';

export interface AccountOpenTermItem {
  id: string;
  title: string;
  category: AccountOpenTermCategory;
  isAgreed: boolean;
}

export interface AccountOpenState {
  currentStep: AccountOpenStep;
  terms: AccountOpenTermItem[];
  selectedAccountTypeUniqueNo: string;
  selectedBankCode: string;
  selectedBankName: string;
  selectedProductName: string;
  customerName: string;
  englishName: string;
  phoneNumber: string;
  zipCode: string;
  address: string;
  addressDetail: string;
  emailLocalPart: string;
  emailDomain: string;
  isSmsAgreed: boolean;
  isAlimtalkAgreed: boolean;
  isMailAgreed: boolean;
  bankingId: string;
  bankingPassword: string;
  bankingPasswordConfirm: string;
  dailyTransferLimit: string;
  verifiedOwnerName: string;
  verifiedOwnerPhoneNumber: string;
  currentUserName: string;
  currentUserPhoneNumber: string;
  isIdentityVerificationPending: boolean;
  createdBankName: string;
  createdAccountNumber: string;
  createdAccountAlias: string;
  isProductLoading: boolean;
  isCreatePending: boolean;
  customerNameError: string | null;
  englishNameError: string | null;
  phoneNumberError: string | null;
  addressError: string | null;
  emailError: string | null;
  bankingIdError: string | null;
  bankingPasswordError: string | null;
  bankingPasswordConfirmError: string | null;
  dailyTransferLimitError: string | null;
  identityVerificationError: string | null;
  lastHandledIdentityVerificationId: string | null;
  errorMessage: string | null;
}
