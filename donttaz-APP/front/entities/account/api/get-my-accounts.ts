import { apiClient } from '@/shared/api';
import type { GetMyAccountsResponse } from '../types/account';

export async function getMyAccountsApi(): Promise<GetMyAccountsResponse> {
  const response = await apiClient.get<GetMyAccountsResponse>('/accounts');
  return response.data;
}
