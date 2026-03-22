import { apiClient } from '@/shared/api';

export async function setPrimaryAccountApi(accountNo: string): Promise<void> {
  await apiClient.patch(`/accounts/${accountNo}/primary`);
}
