import { apiClient } from '@/shared/api';
import type { GetVaultsResponse } from '../types/vault';

export async function getVaultApi(): Promise<GetVaultsResponse> {
  const response = await apiClient.get<GetVaultsResponse>('/api/v1/vaults');
  return response.data;
}
