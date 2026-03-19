import { create } from 'zustand';
import type { SignupFormState, SignupStep } from '../types/signup';

interface SignupStore extends SignupFormState {
  setCurrentStep: (step: SignupStep) => void;
  setErrorMessage: (message: string | null) => void;
  setVerificationLoading: (value: boolean) => void;
  setSignupLoading: (value: boolean) => void;
  setAutoLoginLoading: (value: boolean) => void;
  setVerifiedIdentity: (name: string, phoneNumber: string) => void;
  setLoginId: (value: string) => void;
  setPassword: (value: string) => void;
  setPasswordConfirm: (value: string) => void;
  setPinCode: (value: string) => void;
  setPinCodeConfirm: (value: string) => void;
  setLoginIdError: (value: string | null) => void;
  setPasswordError: (value: string | null) => void;
  setPasswordConfirmError: (value: string | null) => void;
  setPinError: (value: string | null) => void;
  setHasCreatedAccount: (value: boolean) => void;
  resetFlow: () => void;
}

const initialState: SignupFormState = {
  currentStep: 'start',
  isSubmitting: false,
  errorMessage: null,
  verifiedName: '',
  verifiedPhoneNumber: '',
  isVerified: false,
  loginId: '',
  password: '',
  passwordConfirm: '',
  pinCode: '',
  pinCodeConfirm: '',
  loginIdError: null,
  passwordError: null,
  passwordConfirmError: null,
  pinError: null,
  isStartingVerification: false,
  isSigningUp: false,
  isAutoLoggingIn: false,
  hasCreatedAccount: false,
};

export const useSignupStore = create<SignupStore>((set) => ({
  ...initialState,
  setCurrentStep: (currentStep) => set({ currentStep }),
  setErrorMessage: (errorMessage) => set({ errorMessage }),
  setVerificationLoading: (isStartingVerification) => set({ isStartingVerification }),
  setSignupLoading: (isSigningUp) =>
    set((state) => ({ isSigningUp, isSubmitting: isSigningUp || state.isAutoLoggingIn })),
  setAutoLoginLoading: (isAutoLoggingIn) =>
    set((state) => ({ isAutoLoggingIn, isSubmitting: state.isSigningUp || isAutoLoggingIn })),
  setVerifiedIdentity: (verifiedName, verifiedPhoneNumber) =>
    set({
      verifiedName,
      verifiedPhoneNumber,
      isVerified: true,
    }),
  setLoginId: (loginId) => set({ loginId }),
  setPassword: (password) => set({ password }),
  setPasswordConfirm: (passwordConfirm) => set({ passwordConfirm }),
  setPinCode: (pinCode) => set({ pinCode }),
  setPinCodeConfirm: (pinCodeConfirm) => set({ pinCodeConfirm }),
  setLoginIdError: (loginIdError) => set({ loginIdError }),
  setPasswordError: (passwordError) => set({ passwordError }),
  setPasswordConfirmError: (passwordConfirmError) => set({ passwordConfirmError }),
  setPinError: (pinError) => set({ pinError }),
  setHasCreatedAccount: (hasCreatedAccount) => set({ hasCreatedAccount }),
  resetFlow: () => set(initialState),
}));
