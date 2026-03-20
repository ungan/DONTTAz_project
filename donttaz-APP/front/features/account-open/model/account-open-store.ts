import { create } from 'zustand';
import type { AccountOpenState, AccountOpenStep } from '../types/account-open';

const initialTerms = [
  {
    id: 'required-notice',
    title: '필수 안내사항 확인',
    category: 'required' as const,
    isAgreed: false,
  },
  {
    id: 'required-product',
    title: 'DONTAZ 뱅킹 서비스 이용약관',
    category: 'required' as const,
    isAgreed: false,
  },
  {
    id: 'required-privacy',
    title: '개인(신용) 정보 수집 · 이용 동의',
    category: 'required' as const,
    isAgreed: false,
  },
  {
    id: 'required-product-desc',
    title: '상품 이용약관',
    category: 'required' as const,
    isAgreed: false,
  },
  {
    id: 'required-marketing',
    title: '개인(신용) 정보 조회 · 제공 동의',
    category: 'required' as const,
    isAgreed: false,
  },
  {
    id: 'optional-alert',
    title: '입출금서비스(푸시알림) 신청',
    category: 'optional' as const,
    isAgreed: false,
  },
];

interface AccountOpenStore extends AccountOpenState {
  setCurrentStep: (step: AccountOpenStep) => void;
  setErrorMessage: (message: string | null) => void;
  setSelectedProduct: (
    accountTypeUniqueNo: string,
    bankCode: string,
    bankName: string,
    productName: string
  ) => void;
  setCustomerName: (value: string) => void;
  setEnglishName: (value: string) => void;
  setPhoneNumber: (value: string) => void;
  setZipCode: (value: string) => void;
  setAddress: (value: string) => void;
  setAddressDetail: (value: string) => void;
  setEmailLocalPart: (value: string) => void;
  setEmailDomain: (value: string) => void;
  setSmsAgreed: (value: boolean) => void;
  setAlimtalkAgreed: (value: boolean) => void;
  setMailAgreed: (value: boolean) => void;
  setBankingId: (value: string) => void;
  setBankingPassword: (value: string) => void;
  setBankingPasswordConfirm: (value: string) => void;
  setDailyTransferLimit: (value: string) => void;
  setVerifiedOwnerInfo: (name: string, phoneNumber: string) => void;
  setCurrentUserInfo: (name: string, phoneNumber: string) => void;
  setCreatedAccountResult: (bankName: string, accountNumber: string, accountAlias: string) => void;
  setIsProductLoading: (value: boolean) => void;
  setIsIdentityVerificationPending: (value: boolean) => void;
  setIsCreatePending: (value: boolean) => void;
  setCustomerNameError: (value: string | null) => void;
  setEnglishNameError: (value: string | null) => void;
  setPhoneNumberError: (value: string | null) => void;
  setAddressError: (value: string | null) => void;
  setEmailError: (value: string | null) => void;
  setBankingIdError: (value: string | null) => void;
  setBankingPasswordError: (value: string | null) => void;
  setBankingPasswordConfirmError: (value: string | null) => void;
  setDailyTransferLimitError: (value: string | null) => void;
  setIdentityVerificationError: (value: string | null) => void;
  setLastHandledIdentityVerificationId: (value: string | null) => void;
  toggleTerm: (id: string) => void;
  setCategoryAgreed: (category: 'required' | 'optional', isAgreed: boolean) => void;
  resetTerms: () => void;
  resetFlow: () => void;
}

const initialState: AccountOpenState = {
  currentStep: 'terms',
  terms: initialTerms,
  selectedAccountTypeUniqueNo: '',
  selectedBankCode: '',
  selectedBankName: '',
  selectedProductName: '',
  customerName: '',
  englishName: '',
  phoneNumber: '',
  zipCode: '',
  address: '',
  addressDetail: '',
  emailLocalPart: '',
  emailDomain: '',
  isSmsAgreed: false,
  isAlimtalkAgreed: false,
  isMailAgreed: false,
  bankingId: '',
  bankingPassword: '',
  bankingPasswordConfirm: '',
  dailyTransferLimit: '',
  verifiedOwnerName: '',
  verifiedOwnerPhoneNumber: '',
  currentUserName: '',
  currentUserPhoneNumber: '',
  isIdentityVerificationPending: false,
  createdBankName: '',
  createdAccountNumber: '',
  createdAccountAlias: '',
  isProductLoading: false,
  isCreatePending: false,
  customerNameError: null,
  englishNameError: null,
  phoneNumberError: null,
  addressError: null,
  emailError: null,
  bankingIdError: null,
  bankingPasswordError: null,
  bankingPasswordConfirmError: null,
  dailyTransferLimitError: null,
  identityVerificationError: null,
  lastHandledIdentityVerificationId: null,
  errorMessage: null,
};

export const useAccountOpenStore = create<AccountOpenStore>((set) => ({
  ...initialState,
  setCurrentStep: (currentStep) => set({ currentStep }),
  setErrorMessage: (errorMessage) => set({ errorMessage }),
  setSelectedProduct: (selectedAccountTypeUniqueNo, selectedBankCode, selectedBankName, selectedProductName) =>
    set({
      selectedAccountTypeUniqueNo,
      selectedBankCode,
      selectedBankName,
      selectedProductName,
    }),
  setCustomerName: (customerName) => set({ customerName }),
  setEnglishName: (englishName) => set({ englishName }),
  setPhoneNumber: (phoneNumber) => set({ phoneNumber }),
  setZipCode: (zipCode) => set({ zipCode }),
  setAddress: (address) => set({ address }),
  setAddressDetail: (addressDetail) => set({ addressDetail }),
  setEmailLocalPart: (emailLocalPart) => set({ emailLocalPart }),
  setEmailDomain: (emailDomain) => set({ emailDomain }),
  setSmsAgreed: (isSmsAgreed) => set({ isSmsAgreed }),
  setAlimtalkAgreed: (isAlimtalkAgreed) => set({ isAlimtalkAgreed }),
  setMailAgreed: (isMailAgreed) => set({ isMailAgreed }),
  setBankingId: (bankingId) => set({ bankingId }),
  setBankingPassword: (bankingPassword) => set({ bankingPassword }),
  setBankingPasswordConfirm: (bankingPasswordConfirm) => set({ bankingPasswordConfirm }),
  setDailyTransferLimit: (dailyTransferLimit) => set({ dailyTransferLimit }),
  setVerifiedOwnerInfo: (verifiedOwnerName, verifiedOwnerPhoneNumber) =>
    set({ verifiedOwnerName, verifiedOwnerPhoneNumber }),
  setCurrentUserInfo: (currentUserName, currentUserPhoneNumber) =>
    set({ currentUserName, currentUserPhoneNumber }),
  setCreatedAccountResult: (createdBankName, createdAccountNumber, createdAccountAlias) =>
    set({ createdBankName, createdAccountNumber, createdAccountAlias }),
  setIsProductLoading: (isProductLoading) => set({ isProductLoading }),
  setIsIdentityVerificationPending: (isIdentityVerificationPending) =>
    set({ isIdentityVerificationPending }),
  setIsCreatePending: (isCreatePending) => set({ isCreatePending }),
  setCustomerNameError: (customerNameError) => set({ customerNameError }),
  setEnglishNameError: (englishNameError) => set({ englishNameError }),
  setPhoneNumberError: (phoneNumberError) => set({ phoneNumberError }),
  setAddressError: (addressError) => set({ addressError }),
  setEmailError: (emailError) => set({ emailError }),
  setBankingIdError: (bankingIdError) => set({ bankingIdError }),
  setBankingPasswordError: (bankingPasswordError) => set({ bankingPasswordError }),
  setBankingPasswordConfirmError: (bankingPasswordConfirmError) =>
    set({ bankingPasswordConfirmError }),
  setDailyTransferLimitError: (dailyTransferLimitError) => set({ dailyTransferLimitError }),
  setIdentityVerificationError: (identityVerificationError) => set({ identityVerificationError }),
  setLastHandledIdentityVerificationId: (lastHandledIdentityVerificationId) =>
    set({ lastHandledIdentityVerificationId }),
  toggleTerm: (id) =>
    set((state) => ({
      terms: state.terms.map((term) =>
        term.id === id ? { ...term, isAgreed: !term.isAgreed } : term
      ),
    })),
  setCategoryAgreed: (category, isAgreed) =>
    set((state) => ({
      terms: state.terms.map((term) =>
        term.category === category ? { ...term, isAgreed } : term
      ),
    })),
  resetTerms: () =>
    set((state) => ({
      terms: state.terms.map((term) => ({ ...term, isAgreed: false })),
    })),
  resetFlow: () => set(initialState),
}));
