import { create } from 'zustand';
import type { AccountOpenState, AccountOpenStep } from '../types/account-open';

const initialTerms = [
  {
    id: 'required-notice',
    title: '필수 안내사항 확인',
    category: 'required' as const,
    isAgreed: false,
    content: `제1조 (목적)\n본 안내사항은 DONTAZ(이하 "회사")가 제공하는 뱅킹 서비스 이용에 앞서 고객이 반드시 확인해야 할 사항을 안내하기 위한 목적으로 작성되었습니다.\n\n제2조 (서비스 이용 조건)\n본 서비스는 만 19세 이상의 대한민국 국민(내국인)을 대상으로 제공됩니다. 미성년자의 경우 법정대리인의 동의가 필요합니다.\n\n제3조 (본인확인)\n계좌 개설 시 본인확인 절차가 진행됩니다. 타인 명의를 이용한 계좌 개설은 금융실명거래 및 비밀보장에 관한 법률에 의거 처벌받을 수 있습니다.\n\n제4조 (예금자보호)\n본 서비스를 통해 개설된 계좌의 예금은 예금자보호법에 따라 1인당 최고 5,000만 원까지 보호됩니다.\n\n제5조 (주의사항)\n보이스피싱 등 금융사기 피해 예방을 위해 타인의 요청에 의한 계좌 개설, 계좌 양도, 비밀번호 제공 행위는 엄격히 금지됩니다.`,
  },
  {
    id: 'required-product',
    title: 'DONTAZ 뱅킹 서비스 이용약관',
    category: 'required' as const,
    isAgreed: false,
    content: `제1조 (목적)\n본 약관은 DONTAZ(이하 "회사")가 제공하는 뱅킹 서비스(이하 "서비스")의 이용 조건 및 절차, 회사와 이용자 간의 권리·의무 및 책임 사항을 규정함을 목적으로 합니다.\n\n제2조 (약관의 효력 및 변경)\n① 본 약관은 서비스를 이용하고자 하는 모든 이용자에게 공시됨으로써 효력이 발생합니다.\n② 회사는 관계 법령에 위배되지 않는 범위 내에서 본 약관을 변경할 수 있으며, 변경 시 최소 7일 전에 공지합니다.\n\n제3조 (서비스의 제공)\n① 회사는 계좌 조회, 이체, 자산 분석, 소비 플랜 등의 서비스를 제공합니다.\n② 서비스는 연중무휴 24시간 제공을 원칙으로 하되, 시스템 점검 등의 사유로 일시 중단될 수 있습니다.\n\n제4조 (이용자의 의무)\n① 이용자는 관계 법령, 본 약관 및 회사의 안내 사항을 준수해야 합니다.\n② 이용자는 자신의 계정 정보를 타인과 공유하거나 양도할 수 없습니다.\n\n제5조 (책임의 한계)\n회사는 천재지변, 전쟁, 기간통신사업자의 서비스 중단 등 불가항력적 사유로 인한 서비스 중단에 대해 책임을 지지 않습니다.`,
  },
  {
    id: 'required-privacy',
    title: '개인(신용) 정보 수집 · 이용 동의',
    category: 'required' as const,
    isAgreed: false,
    content: `■ 수집·이용 목적\n- 계좌 개설 및 금융 서비스 제공\n- 본인확인 및 부정 이용 방지\n- 금융사고 조사·예방 및 법령상 의무 이행\n- 서비스 개선 및 신규 서비스 개발\n\n■ 수집·이용 항목\n- 필수항목: 성명, 생년월일, 성별, 내/외국인 구분, 연락처(휴대폰 번호), 이메일 주소, 주소, 계좌번호, 거래 정보\n- 선택항목: 마케팅 수신 동의 여부\n\n■ 보유 및 이용 기간\n- 서비스 이용 계약 종료 후 5년 (전자금융거래법 등 관계 법령에 따름)\n- 단, 관계 법령에 따라 보존할 필요가 있는 경우 해당 법령에서 정한 기간 동안 보유\n\n■ 동의 거부 권리 및 불이익\n귀하는 개인(신용)정보 수집·이용에 대한 동의를 거부하실 수 있습니다. 다만, 동의를 거부하시는 경우 계좌 개설 및 금융 서비스 이용이 제한될 수 있습니다.`,
  },
  {
    id: 'required-product-desc',
    title: '상품 이용약관',
    category: 'required' as const,
    isAgreed: false,
    content: `제1조 (상품 개요)\n본 약관은 DONTAZ 입출금 계좌(이하 "본 상품")의 이용에 관한 사항을 정합니다.\n\n제2조 (가입 대상)\n만 19세 이상의 실명 확인된 개인 고객\n\n제3조 (계좌 개설)\n① 계좌 개설은 1인 1계좌를 원칙으로 합니다.\n② 계좌 개설 시 본 약관 및 관련 약관에 동의해야 합니다.\n\n제4조 (입출금)\n① 입금은 제한 없이 가능합니다.\n② 출금은 잔액 범위 내에서 가능하며, 1일 이체 한도는 고객이 설정한 금액 이내로 제한됩니다.\n\n제5조 (이자)\n본 상품은 무이자 입출금 상품입니다.\n\n제6조 (계좌 해지)\n① 고객은 언제든지 계좌를 해지할 수 있습니다.\n② 계좌 해지 시 잔액은 지정 계좌로 자동 이체됩니다.\n\n제7조 (휴면 계좌)\n1년 이상 거래가 없는 계좌는 서면 통지 후 휴면 계좌로 전환될 수 있습니다.`,
  },
  {
    id: 'required-marketing',
    title: '개인(신용) 정보 조회 · 제공 동의',
    category: 'required' as const,
    isAgreed: false,
    content: `■ 조회·제공 목적\n- 계좌 개설 및 금융 거래 관련 본인확인\n- 신용정보 조회 및 금융사고 예방\n- 법령에 따른 금융정보 제공 의무 이행\n\n■ 조회·제공 대상 기관\n- 신용정보회사(NICE평가정보, KCB 등)\n- 금융결제원\n- 관계 법령에 따른 금융 당국 (금융감독원, 금융정보분석원 등)\n\n■ 조회·제공 항목\n- 성명, 생년월일, 연락처, 계좌정보, 거래 이력\n\n■ 보유 및 이용 기간\n- 동의일로부터 서비스 이용 계약 종료 후 5년\n\n■ 동의 거부 권리\n귀하는 본 동의를 거부하실 수 있으나, 거부 시 계좌 개설이 제한됩니다.`,
  },
  {
    id: 'optional-alert',
    title: '입출금서비스(푸시알림) 신청',
    category: 'optional' as const,
    isAgreed: false,
    content: `■ 서비스 개요\n입출금 내역 발생 시 등록된 모바일 기기로 푸시 알림을 발송하는 서비스입니다.\n\n■ 알림 발송 시점\n- 입금 발생 시\n- 출금(이체 포함) 발생 시\n- 이체 한도 초과 시도 발생 시\n\n■ 신청 방법\n본 동의에 동의하시면 앱 설치 시 등록된 기기로 자동 발송됩니다.\n\n■ 해지 방법\n앱 내 설정 > 알림 설정에서 언제든지 해지 가능합니다.\n\n■ 유의사항\n- 기기 변경, 앱 재설치 시 재신청이 필요할 수 있습니다.\n- 본 서비스는 선택 사항으로, 동의하지 않아도 계좌 개설이 가능합니다.`,
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
