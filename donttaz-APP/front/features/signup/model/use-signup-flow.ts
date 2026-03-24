import { router } from 'expo-router';
import { isAxiosError } from 'axios';
import { completeLoginSession, loginApi } from '@/features/auth';
import { storeDevicePin } from '@/shared/api/auth-storage';
import { signupApi, verifyIdentityApi } from '../api/signup';
import { parsePortOneCallbackUrl, requestPortOneIdentityVerification } from '../api/portone';
import { useSignupStore } from './signup-store';
import type { SignupRequest, SignupStep, VerifyIdentityResponseData } from '../types/signup';

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const PASSWORD_REGEX = /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_=+~]).{8,16}$/;
const MOCK_VERIFICATION_ID = 'mock-verification-id';
const DEFAULT_DEVICE_ID = 'donttaz-expo';

function getFallbackVerificationData(): VerifyIdentityResponseData {
  return {
    name: '홍길동',
    phoneNumber: '01012345678',
    isRegistered: false,
    maskedEmail: null,
  };
}

function getSignupErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    if (error.response?.status === 409) {
      return '이미 가입된 이메일 또는 전화번호입니다.';
    }

    if (error.response?.status === 400) {
      return '입력값 형식을 다시 확인해 주세요.';
    }

    return error.response?.data?.message ?? '회원가입 처리 중 문제가 발생했습니다.';
  }

  return '회원가입 처리 중 문제가 발생했습니다.';
}

export function useSignupFlow() {
  const signupState = useSignupStore();

  const moveToStep = (
    step: SignupStep,
    path: '/signup-start' | '/signup-account' | '/signup-pin' | '/signup-complete'
  ) => {
    signupState.setCurrentStep(step);
    router.push(path as never);
  };

  const resetFromStart = () => {
    signupState.resetFlow();
    signupState.setCurrentStep('start');
  };

  const applyVerifiedIdentity = (verifiedData: VerifyIdentityResponseData) => {
    if (verifiedData.isRegistered) {
      signupState.setErrorMessage(
        verifiedData.maskedEmail
          ? `이미 가입된 계정입니다. ${verifiedData.maskedEmail} 계정을 확인해 주세요.`
          : '이미 가입된 사용자입니다.'
      );
      return false;
    }

    signupState.setVerifiedIdentity(verifiedData.name, verifiedData.phoneNumber);
    moveToStep('account', '/signup-account');
    return true;
  };

  const completeIdentityVerification = async (identityVerificationId: string) => {
    let verifiedData = getFallbackVerificationData();

    if (__DEV__) {
      console.log('[SignupDebug][Flow] completeIdentityVerification:start', {
        identityVerificationId,
      });
    }

    const response = await verifyIdentityApi({
      identityVerificationId,
    });
    verifiedData = response.data;

    if (__DEV__) {
      console.log('[SignupDebug][Flow] completeIdentityVerification:success', verifiedData);
    }

    applyVerifiedIdentity(verifiedData);
  };

  const startVerification = async () => {
    signupState.setErrorMessage(null);
    signupState.setVerificationLoading(true);

    if (__DEV__) {
      console.log('[SignupDebug][Flow] startVerification:start', {
        isMock: process.env.EXPO_PUBLIC_IS_MOCK,
        baseUrl: process.env.EXPO_PUBLIC_BASE_URL,
      });
    }

    try {
      let identityVerificationId = MOCK_VERIFICATION_ID;

      if (process.env.EXPO_PUBLIC_IS_MOCK !== 'true') {
        if (__DEV__) {
          console.log('[SignupDebug][Flow] startVerification:beforePortOne');
        }

        const portOneResult = await requestPortOneIdentityVerification();
        identityVerificationId = portOneResult.identityVerificationId;

        if (__DEV__) {
          console.log('[SignupDebug][Flow] startVerification:afterPortOne', {
            identityVerificationId,
          });
        }
      }

      await completeIdentityVerification(identityVerificationId);
    } catch (error) {
      if (__DEV__) {
        console.log('[SignupDebug][Flow] startVerification:error', {
          error,
          message: error instanceof Error ? error.message : String(error),
        });
      }

      signupState.setErrorMessage(
        error instanceof Error ? error.message : '본인인증 처리 중 문제가 발생했습니다.'
      );
    } finally {
      signupState.setVerificationLoading(false);
    }
  };

  const completeVerificationFromCallbackUrl = async (url: string) => {
    if (__DEV__) {
      console.log('[SignupDebug][Flow] completeVerificationFromCallbackUrl:start', { url });
    }

    const { identityVerificationId, errorCode, errorMessage } = parsePortOneCallbackUrl(url);

    if (identityVerificationId && identityVerificationId.length > 0) {
      signupState.setErrorMessage(null);
      signupState.setVerificationLoading(true);

      try {
        await completeIdentityVerification(identityVerificationId);
      } catch (error) {
        signupState.setErrorMessage(
          error instanceof Error ? error.message : '본인인증 처리 중 문제가 발생했습니다.'
        );
      } finally {
        signupState.setVerificationLoading(false);
      }

      return true;
    }

    if (errorMessage && errorMessage.length > 0) {
      if (__DEV__) {
        console.log('[SignupDebug][Flow] completeVerificationFromCallbackUrl:error', {
          errorCode,
          errorMessage,
        });
      }

      signupState.setErrorMessage(
        errorCode && errorCode.length > 0 ? `[${errorCode}] ${errorMessage}` : errorMessage
      );
      signupState.setVerificationLoading(false);
      return true;
    }

    return false;
  };

  const updateLoginId = (value: string) => {
    signupState.setLoginId(value);
    if (signupState.loginIdError) {
      signupState.setLoginIdError(null);
    }
    if (signupState.errorMessage) {
      signupState.setErrorMessage(null);
    }
  };

  const updatePassword = (value: string) => {
    signupState.setPassword(value);
    if (signupState.passwordError) {
      signupState.setPasswordError(null);
    }
    if (signupState.passwordConfirmError) {
      signupState.setPasswordConfirmError(null);
    }
  };

  const updatePasswordConfirm = (value: string) => {
    signupState.setPasswordConfirm(value);
    if (signupState.passwordConfirmError) {
      signupState.setPasswordConfirmError(null);
    }
  };

  const submitAccountStep = () => {
    const trimmedLoginId = signupState.loginId.trim();
    let hasError = false;

    signupState.setErrorMessage(null);
    signupState.setLoginIdError(null);
    signupState.setPasswordError(null);
    signupState.setPasswordConfirmError(null);

    if (!trimmedLoginId) {
      signupState.setLoginIdError('이메일을 입력해 주세요.');
      hasError = true;
    } else if (!EMAIL_REGEX.test(trimmedLoginId)) {
      signupState.setLoginIdError('올바른 이메일 형식을 입력해 주세요.');
      hasError = true;
    }

    if (!signupState.password) {
      signupState.setPasswordError('비밀번호를 입력해 주세요.');
      hasError = true;
    } else if (!PASSWORD_REGEX.test(signupState.password)) {
      signupState.setPasswordError('영문, 숫자, 특수문자를 포함한 8~16자를 입력해 주세요.');
      hasError = true;
    }

    if (!signupState.passwordConfirm) {
      signupState.setPasswordConfirmError('비밀번호 확인을 입력해 주세요.');
      hasError = true;
    } else if (signupState.password !== signupState.passwordConfirm) {
      signupState.setPasswordConfirmError('비밀번호가 일치하지 않습니다.');
      hasError = true;
    }

    if (hasError) {
      return false;
    }

    signupState.setCurrentStep('pin');
    router.push('/signup-pin' as never);
    return true;
  };

  const handlePinKeyPress = (
    key: '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '00' | 'X'
  ) => {
    signupState.setPinError(null);
    signupState.setErrorMessage(null);

    if (key === 'X') {
      if (signupState.pinCodeConfirm.length > 0) {
        signupState.setPinCodeConfirm(signupState.pinCodeConfirm.slice(0, -1));
        return;
      }

      if (signupState.pinCode.length > 0) {
        signupState.setPinCode(signupState.pinCode.slice(0, -1));
      }
      return;
    }

    if (key === '00') {
      return;
    }

    if (signupState.pinCode.length < 6) {
      signupState.setPinCode(`${signupState.pinCode}${key}`);
      return;
    }

    if (signupState.pinCodeConfirm.length < 6) {
      signupState.setPinCodeConfirm(`${signupState.pinCodeConfirm}${key}`);
    }
  };

  const resetPinEntry = () => {
    signupState.setPinCode('');
    signupState.setPinCodeConfirm('');
    signupState.setPinError(null);
    signupState.setErrorMessage(null);
  };

  const submitPinStep = () => {
    signupState.setPinError(null);
    signupState.setErrorMessage(null);

    if (signupState.pinCode.length !== 6) {
      signupState.setPinError('PIN 6자리를 모두 입력해 주세요.');
      return false;
    }

    if (signupState.pinCodeConfirm.length !== 6) {
      signupState.setPinError('PIN 확인 6자리를 모두 입력해 주세요.');
      return false;
    }

    if (signupState.pinCode !== signupState.pinCodeConfirm) {
      signupState.setPinError('PIN이 일치하지 않습니다. 다시 입력해 주세요.');
      return false;
    }

    signupState.setCurrentStep('complete');
    router.push('/signup-complete' as never);
    return true;
  };

  const buildSignupRequest = (): SignupRequest => ({
    email: signupState.loginId.trim(),
    password: signupState.password,
    pinCode: signupState.pinCode,
    name: signupState.verifiedName,
    phoneNumber: signupState.verifiedPhoneNumber.replace(/[^0-9]/g, ''),
    deviceId: DEFAULT_DEVICE_ID,
  });

  const submitSignupAndAutoLogin = async () => {
    const latestState = useSignupStore.getState();

    if (latestState.isSigningUp || latestState.isAutoLoggingIn) {
      return;
    }

    signupState.setErrorMessage(null);

    try {
      if (!latestState.hasCreatedAccount) {
        signupState.setSignupLoading(true);
        const signupBody = buildSignupRequest();
        console.log('[SIGNUP REQUEST]', JSON.stringify(signupBody));
        await signupApi(signupBody);
        signupState.setHasCreatedAccount(true);
      }
    } catch (error) {
      signupState.setErrorMessage(getSignupErrorMessage(error));
      return;
    } finally {
      signupState.setSignupLoading(false);
    }

    try {
      signupState.setAutoLoginLoading(true);
      const response = await loginApi({
        loginId: signupState.loginId.trim(),
        password: signupState.password,
      });

      await storeDevicePin(signupState.pinCode);
      await completeLoginSession(response.data);
    } catch {
      signupState.setErrorMessage(
        '회원가입은 완료되었지만 자동 로그인에 실패했습니다. 다시 시도해 주세요.'
      );
    } finally {
      signupState.setAutoLoginLoading(false);
    }
  };

  return {
    ...signupState,
    resetFromStart,
    startVerification,
    completeVerificationFromCallbackUrl,
    updateLoginId,
    updatePassword,
    updatePasswordConfirm,
    submitAccountStep,
    handlePinKeyPress,
    resetPinEntry,
    submitPinStep,
    submitSignupAndAutoLogin,
  };
}
