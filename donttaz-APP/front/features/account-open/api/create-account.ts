import { apiClient } from '@/shared/api';
import type {
  AccountOpenApiResponse,
  CreateAccountRequest,
  CreateAccountResponseData,
} from '../types/account-open-api';

export async function createAccountApi(body: CreateAccountRequest) {
  const response = await apiClient.post<AccountOpenApiResponse<CreateAccountResponseData>>(
    '/accounts',
    body
  );

  return response.data;
}
