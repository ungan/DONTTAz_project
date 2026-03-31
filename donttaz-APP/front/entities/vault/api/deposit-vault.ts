import { apiClient } from '@/shared/api';
import type { DepositVaultRequest, DepositVaultResponse, VaultKind } from '../types/vault';

export async function depositVaultApi(
  kind: VaultKind,
  vaultId: number,
  body: DepositVaultRequest
): Promise<DepositVaultResponse> {
  const response = await apiClient.post<DepositVaultResponse>(
    `/vaults/${kind}/${vaultId}/deposit`,
    body
  );
  return response.data;
}
