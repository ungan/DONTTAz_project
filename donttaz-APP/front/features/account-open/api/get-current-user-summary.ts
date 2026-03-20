import { apiClient } from '@/shared/api';
import type {
  AccountOpenApiResponse,
  CurrentUserSummaryResponseData,
} from '../types/account-open-api';

export async function getCurrentUserSummaryApi() {
  const response = await apiClient.get<AccountOpenApiResponse<CurrentUserSummaryResponseData>>(
    '/users/me'
  );

  return response.data;
}
