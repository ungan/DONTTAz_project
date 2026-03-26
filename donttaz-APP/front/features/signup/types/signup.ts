export const SIGNUP_STEP_ORDER = ['start', 'account', 'pin', 'complete'] as const;

export type SignupStep = (typeof SIGNUP_STEP_ORDER)[number];

export interface SignupApiResponse<T> {
  status: string;
  message: string;
  data: T;
}

export interface VerifyIdentityRequest {
  identityVerificationId: string;
}

export interface PortOneVerificationResult {
  identityVerificationId: string;
}

export interface VerifyIdentityResponseData {
  name: string;
  phoneNumber: string;
  isRegistered: boolean;
  maskedEmail: string | null;
}

export interface SignupRequest {
  email: string;
  password: string;
  pinCode: string;
  name: string;
  phoneNumber: string;
  deviceId?: string;
}

export interface SignupResponseData {
  userId: number;
  email: string;
  name: string;
}

export interface SignupFormState {
  currentStep: SignupStep;
  isSubmitting: boolean;
  errorMessage: string | null;
  lastHandledIdentityVerificationId: string | null;
  verifiedName: string;
  verifiedPhoneNumber: string;
  isVerified: boolean;
  loginId: string;
  password: string;
  passwordConfirm: string;
  pinCode: string;
  pinCodeConfirm: string;
  loginIdError: string | null;
  passwordError: string | null;
  passwordConfirmError: string | null;
  pinError: string | null;
  isStartingVerification: boolean;
  isSigningUp: boolean;
  isAutoLoggingIn: boolean;
  hasCreatedAccount: boolean;
}
