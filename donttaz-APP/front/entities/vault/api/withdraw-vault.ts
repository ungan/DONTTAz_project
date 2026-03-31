import { apiClient } from '@/shared/api';
import type { VaultKind, WithdrawVaultRequest, WithdrawVaultResponse } from '../types/vault';

export async function withdrawVaultApi(
  kind: VaultKind,
  vaultId: number,
  body: WithdrawVaultRequest
): Promise<WithdrawVaultResponse> {
  const response = await apiClient.post<{ data: WithdrawVaultResponse }>(
    `/vaults/${kind}/${vaultId}/withdraw`,
    body
  );
  return response.data.data;
}
