import { apiClient } from '@/shared/api';
import type { GetVaultTransactionsResponse } from '../types/vault';

export async function getVaultTransactionsApi(vaultId: number): Promise<GetVaultTransactionsResponse> {
  const response = await apiClient.get<GetVaultTransactionsResponse>(
    `/vaults/${vaultId}/history`
  );
  return response.data;
}
