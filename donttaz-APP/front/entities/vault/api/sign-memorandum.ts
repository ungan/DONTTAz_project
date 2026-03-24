import { apiClient } from '@/shared/api';

export async function signMemorandumApi(vaultId: number): Promise<void> {
  await apiClient.post(`/memorandums/${vaultId}/sign`);
}
