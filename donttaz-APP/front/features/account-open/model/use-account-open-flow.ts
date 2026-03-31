import { isAxiosError } from 'axios';
import { router } from 'expo-router';
import { useBankAccountStore } from '@/entities/bank-account';
import { createAccountApi } from '../api/create-account';
import { getCurrentUserSummaryApi } from '../api/get-current-user-summary';
import { getDemandDepositProductsApi } from '../api/get-demand-deposit-products';
import { useAccountOpenStore } from './account-open-store';
import { setPrimaryAccountApi } from '../api/set-primary-account';
import { verifyIdentityApi } from '@/features/signup/api/signup';

const PHONE_REGEX = /^01[0-9]\d{7,8}$/;
const BANKING_ID_REGEX = /^[a-zA-Z0-9]{6,12}$/;
const BANKING_PASSWORD_REGEX = /^[0-9]{4}$/;

function buildAccountAlias(customerName: string) {
  return customerName ? `${customerName} 통장` : '나의 통장';
}

function normalizeName(value: string) {
  return value.trim();
}

function normalizePhoneNumber(value: string) {
  return value.replace(/[^0-9]/g, '');
}

function getAccountOpenErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? '계좌 개설 처리 중 문제가 발생했습니다.';
  }

  return '계좌 개설 처리 중 문제가 발생했습니다.';
}

export function useAccountOpenFlow() {
  const accountOpenState = useAccountOpenStore();

  const requiredTerms = accountOpenState.terms.filter((term) => term.category === 'required');
  const optionalTerms = accountOpenState.terms.filter((term) => term.category === 'optional');

  const isRequiredTermsAgreed =
    requiredTerms.length > 0 && requiredTerms.every((term) => term.isAgreed);
  const isOptionalTermsAgreed =
    optionalTerms.length > 0 && optionalTerms.every((term) => term.isAgreed);

  const startFlow = () => {
    void (async () => {
      accountOpenState.resetFlow();
      accountOpenState.setCurrentStep('terms');
      accountOpenState.setIsProductLoading(true);

      try {
        const response = await getDemandDepositProductsApi();
        const firstProduct = response.data.products[0];

        if (!firstProduct) {
          accountOpenState.setErrorMessage('계좌 상품 정보를 찾을 수 없습니다.');
          return;
        }

        accountOpenState.setSelectedProduct(
          firstProduct.accountTypeUniqueNo,
          firstProduct.bankCode,
          firstProduct.bankName,
          firstProduct.accountName
        );
        accountOpenState.setErrorMessage(null);
      } catch (error) {
        accountOpenState.setErrorMessage(getAccountOpenErrorMessage(error));
      } finally {
        accountOpenState.setIsProductLoading(false);
      }
    })();
  };

  const toggleTerm = (id: string) => {
    accountOpenState.setErrorMessage(null);
    accountOpenState.toggleTerm(id);
  };

  const toggleRequiredTerms = () => {
    accountOpenState.setErrorMessage(null);
    accountOpenState.setCategoryAgreed('required', !isRequiredTermsAgreed);
  };

  const toggleOptionalTerms = () => {
    accountOpenState.setErrorMessage(null);
    accountOpenState.setCategoryAgreed('optional', !isOptionalTermsAgreed);
  };

  const submitTermsStep = () => {
    if (!accountOpenState.selectedAccountTypeUniqueNo) {
      accountOpenState.setErrorMessage('계좌 상품 정보를 불러온 뒤 다시 시도해주세요.');
      return false;
    }

    if (!isRequiredTermsAgreed) {
      accountOpenState.setErrorMessage('필수 약관에 모두 동의해주세요.');
      return false;
    }

    accountOpenState.setCurrentStep('verify');
    router.push('/account-open-verify' as never);
    return true;
  };

  const updateCustomerName = (value: string) => {
    accountOpenState.setCustomerName(value);
    if (accountOpenState.customerNameError) {
      accountOpenState.setCustomerNameError(null);
    }
  };

  const updatePhoneNumber = (value: string) => {
    const sanitizedValue = value.replace(/[^0-9]/g, '');
    accountOpenState.setPhoneNumber(sanitizedValue);
    if (accountOpenState.phoneNumberError) {
      accountOpenState.setPhoneNumberError(null);
    }
  };

  const updateZipCode = (value: string) => {
    accountOpenState.setZipCode(value.replace(/[^0-9]/g, ''));
    if (accountOpenState.addressError) {
      accountOpenState.setAddressError(null);
    }
  };

  const updateAddressDetail = (value: string) => {
    accountOpenState.setAddressDetail(value);
  };

  const toggleSmsAgreed = () => {
    accountOpenState.setSmsAgreed(!accountOpenState.isSmsAgreed);
  };

  const toggleAlimtalkAgreed = () => {
    accountOpenState.setAlimtalkAgreed(!accountOpenState.isAlimtalkAgreed);
  };

  const toggleMailAgreed = () => {
    accountOpenState.setMailAgreed(!accountOpenState.isMailAgreed);
  };

  const moveToProfileStep = () => {
    accountOpenState.setCurrentStep('profile');
  };

  const submitProfileStep = () => {
    let hasError = false;

    accountOpenState.setErrorMessage(null);
    accountOpenState.setCustomerNameError(null);
    accountOpenState.setPhoneNumberError(null);
    accountOpenState.setAddressError(null);

    if (!accountOpenState.verifiedOwnerName.trim()) {
      accountOpenState.setErrorMessage('본인인증을 먼저 완료해주세요.');
      hasError = true;
    }

    if (!accountOpenState.customerName.trim()) {
      accountOpenState.setCustomerNameError('이름을 입력해주세요.');
      hasError = true;
    }

    if (!PHONE_REGEX.test(accountOpenState.phoneNumber.trim())) {
      accountOpenState.setPhoneNumberError('휴대전화번호를 숫자로 올바르게 입력해주세요.');
      hasError = true;
    }

    if (!accountOpenState.zipCode.trim()) {
      accountOpenState.setAddressError('우편번호를 입력해주세요.');
      hasError = true;
    }

    if (hasError) {
      return false;
    }

    accountOpenState.setCurrentStep('banking');
    router.push('/account-open-banking' as never);
    return true;
  };

  const moveToBankingStep = () => {
    accountOpenState.setCurrentStep('banking');
  };

  const updateBankingId = (value: string) => {
    accountOpenState.setBankingId(value.replace(/[^a-zA-Z0-9]/g, ''));
    if (accountOpenState.bankingIdError) {
      accountOpenState.setBankingIdError(null);
    }
  };

  const updateBankingPassword = (value: string) => {
    accountOpenState.setBankingPassword(value.replace(/[^0-9]/g, '').slice(0, 4));
    if (accountOpenState.bankingPasswordError) {
      accountOpenState.setBankingPasswordError(null);
    }
    if (accountOpenState.bankingPasswordConfirmError) {
      accountOpenState.setBankingPasswordConfirmError(null);
    }
  };

  const updateBankingPasswordConfirm = (value: string) => {
    accountOpenState.setBankingPasswordConfirm(value.replace(/[^0-9]/g, '').slice(0, 4));
    if (accountOpenState.bankingPasswordConfirmError) {
      accountOpenState.setBankingPasswordConfirmError(null);
    }
  };

  const updateDailyTransferLimit = (value: string) => {
    accountOpenState.setDailyTransferLimit(value);
    if (accountOpenState.dailyTransferLimitError) {
      accountOpenState.setDailyTransferLimitError(null);
    }
  };

  const submitBankingStep = () => {
    let hasError = false;

    accountOpenState.setErrorMessage(null);
    accountOpenState.setBankingIdError(null);
    accountOpenState.setBankingPasswordError(null);
    accountOpenState.setBankingPasswordConfirmError(null);
    accountOpenState.setDailyTransferLimitError(null);

    if (!BANKING_ID_REGEX.test(accountOpenState.bankingId.trim())) {
      accountOpenState.setBankingIdError('뱅킹 ID는 영문/숫자 6~12자로 입력해주세요.');
      hasError = true;
    }

    if (!BANKING_PASSWORD_REGEX.test(accountOpenState.bankingPassword)) {
      accountOpenState.setBankingPasswordError('뱅킹 비밀번호 4자리를 입력해주세요.');
      hasError = true;
    }

    if (accountOpenState.bankingPassword !== accountOpenState.bankingPasswordConfirm) {
      accountOpenState.setBankingPasswordConfirmError('비밀번호가 일치하지 않습니다.');
      hasError = true;
    }

    if (!accountOpenState.dailyTransferLimit) {
      accountOpenState.setDailyTransferLimitError('1일 이체한도를 선택해주세요.');
      hasError = true;
    }

    if (hasError) {
      return false;
    }

    return completeAccountCreation();
  };

  const moveToVerifyStep = () => {
    accountOpenState.setCurrentStep('verify');
  };

  const proceedToProfileStep = () => {
    accountOpenState.setCurrentStep('banking');
    router.push('/account-open-banking' as never);
  };

  const completeAccountCreation = async () => {
    if (!accountOpenState.selectedAccountTypeUniqueNo) {
      accountOpenState.setErrorMessage('계좌 상품 정보를 찾을 수 없습니다. 처음부터 다시 시도해주세요.');
      return false;
    }

    accountOpenState.setIsCreatePending(true);

    try {
      const response = await createAccountApi({
        accountTypeUniqueNo: accountOpenState.selectedAccountTypeUniqueNo,
      });
      const createdBankName = accountOpenState.selectedBankName || 'DONTTAZ';
      const createdAccountAlias = buildAccountAlias(accountOpenState.customerName);

      await setPrimaryAccountApi(response.data.accountNo);

      useBankAccountStore.getState().setAccount(
        response.data.accountId ?? null,
        createdBankName,
        0,
        response.data.accountNo,
        accountOpenState.selectedProductName || createdAccountAlias
      );

      accountOpenState.setCreatedAccountResult(
        accountOpenState.selectedProductName || createdBankName || 'DONTTAZ 예금',
        response.data.accountNo,
        createdAccountAlias
      );
      accountOpenState.setCurrentStep('complete');
      router.push('/account-open-complete' as never);
      return true;
    } catch (error) {
      accountOpenState.setErrorMessage(getAccountOpenErrorMessage(error));
      return false;
    } finally {
      accountOpenState.setIsCreatePending(false);
    }
  };

  const verifyCurrentUserOwnership = async (identityVerificationId: string) => {
    const latestState = useAccountOpenStore.getState();

    if (latestState.lastHandledIdentityVerificationId === identityVerificationId) {
      return true;
    }

    useAccountOpenStore.getState().setLastHandledIdentityVerificationId(identityVerificationId);
    useAccountOpenStore.getState().setIdentityVerificationError(null);
    useAccountOpenStore.getState().setErrorMessage(null);
    useAccountOpenStore.getState().setIsIdentityVerificationPending(true);

    try {
      const [verifiedResponse, currentUserResponse] = await Promise.all([
        verifyIdentityApi({ identityVerificationId }),
        getCurrentUserSummaryApi(),
      ]);

      const verifiedName = normalizeName(verifiedResponse.data.name);
      const verifiedPhoneNumber = normalizePhoneNumber(verifiedResponse.data.phoneNumber);
      const currentUserName = normalizeName(currentUserResponse.data.name);
      const currentUserPhoneNumber = normalizePhoneNumber(
        currentUserResponse.data.phoneNumber ?? ''
      );

      useAccountOpenStore.getState().setVerifiedOwnerInfo(verifiedName, verifiedPhoneNumber);
      useAccountOpenStore.getState().setCurrentUserInfo(currentUserName, currentUserPhoneNumber);

      if (!currentUserPhoneNumber) {
        useAccountOpenStore.getState().setErrorMessage(
          '현재 사용자 휴대폰 번호를 확인할 수 없습니다. /users/me 응답에 phoneNumber가 필요합니다.'
        );
        return false;
      }

      if (verifiedName !== currentUserName || verifiedPhoneNumber !== currentUserPhoneNumber) {
        useAccountOpenStore.getState().setIdentityVerificationError(
          '본인인증 결과가 현재 로그인한 사용자 정보와 일치하지 않습니다.'
        );
        return false;
      }

      useAccountOpenStore.getState().setCustomerName(verifiedName);
      useAccountOpenStore.getState().setPhoneNumber(verifiedPhoneNumber);
      return true;
    } catch (error) {
      useAccountOpenStore.getState().setErrorMessage(getAccountOpenErrorMessage(error));
      return false;
    } finally {
      useAccountOpenStore.getState().setIsIdentityVerificationPending(false);
    }
  };

  const completeVerificationFromIdentityVerificationId = async (
    identityVerificationId: string
  ) => {
    return verifyCurrentUserOwnership(identityVerificationId);
  };

  const failVerification = (message: string) => {
    accountOpenState.setIdentityVerificationError(message);
    accountOpenState.setIsIdentityVerificationPending(false);
  };

  const submitVerifyStep = async () => {
    if (!accountOpenState.selectedAccountTypeUniqueNo) {
      accountOpenState.setErrorMessage('계좌 상품 정보를 찾을 수 없습니다. 처음부터 다시 시도해주세요.');
      return false;
    }

    accountOpenState.setIdentityVerificationError(null);
    accountOpenState.setErrorMessage(null);
    router.push('/account-open-certification' as never);
    return true;
  };

  return {
    ...accountOpenState,
    requiredTerms,
    optionalTerms,
    isRequiredTermsAgreed,
    isOptionalTermsAgreed,
    startFlow,
    toggleTerm,
    toggleRequiredTerms,
    toggleOptionalTerms,
    submitTermsStep,
    updateCustomerName,
    updatePhoneNumber,
    updateZipCode,
    updateAddressDetail,
    toggleSmsAgreed,
    toggleAlimtalkAgreed,
    toggleMailAgreed,
    moveToProfileStep,
    submitProfileStep,
    moveToBankingStep,
    updateBankingId,
    updateBankingPassword,
    updateBankingPasswordConfirm,
    updateDailyTransferLimit,
    submitBankingStep,
    moveToVerifyStep,
    proceedToProfileStep,
    completeVerificationFromIdentityVerificationId,
    failVerification,
    submitVerifyStep,
  };
}
