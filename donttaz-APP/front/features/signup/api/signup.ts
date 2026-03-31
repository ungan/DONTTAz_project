import { apiClient } from '@/shared/api';
import type {
  SignupApiResponse,
  SignupRequest,
  SignupResponseData,
  VerifyIdentityRequest,
  VerifyIdentityResponseData,
} from '../types/signup';

export async function verifyIdentityApi(body: VerifyIdentityRequest) {
  const response = await apiClient.post<SignupApiResponse<VerifyIdentityResponseData>>(
    '/auth/verify-identity',
    body
  );

  return response.data;
}

export async function signupApi(body: SignupRequest) {
  const response = await apiClient.post<SignupApiResponse<SignupResponseData>>(
    '/users/signup',
    body
  );

  return response.data;
}
