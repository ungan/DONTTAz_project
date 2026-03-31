import { apiClient } from '@/shared/api';

export async function createDefaultVaultApi(): Promise<void> {
  await apiClient.post('/vaults/default');
}
