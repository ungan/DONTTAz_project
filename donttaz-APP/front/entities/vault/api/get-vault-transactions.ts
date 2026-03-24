import { apiClient } from '@/shared/api';
import type { GetVaultTransactionsResponse, VaultKind } from '../types/vault';

export async function getVaultTransactionsApi(
  kind: VaultKind,
  vaultId: number
): Promise<GetVaultTransactionsResponse> {
  const response = await apiClient.get<GetVaultTransactionsResponse>(
    `/vaults/${kind}/${vaultId}/history`
  );
  return response.data;
}
