import { apiClient } from '@/shared/api';
import type { WithdrawVaultResponse } from '../types/vault';

export async function terminateVaultApi(vaultId: number): Promise<WithdrawVaultResponse> {
  const response = await apiClient.post<{ data: WithdrawVaultResponse }>(
    `/vaults/wishlist/${vaultId}/terminate`
  );
  return response.data.data;
}
