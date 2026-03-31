import { apiClient } from '@/shared/api';
import type { GetVaultsResponse } from '../types/vault';

export async function getVaultApi(): Promise<GetVaultsResponse> {
  const response = await apiClient.get<{ data: GetVaultsResponse }>('/vaults');
  return response.data.data;
}
