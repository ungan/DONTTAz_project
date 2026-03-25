import { apiClient } from '@/shared/api';
import type { AccountSummary } from '../types/account';

interface GetPrimaryAccountResponse {
  status: string;
  message: string;
  data: AccountSummary;
}

export async function getPrimaryAccountApi(): Promise<AccountSummary> {
  const response = await apiClient.get<GetPrimaryAccountResponse>('/accounts/primary');
  return response.data.data;
}
