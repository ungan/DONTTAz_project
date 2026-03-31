import { apiClient } from '@/shared/api';
import type { TransferRequest, TransferResponse } from '../types/transfer';

export async function transferApi(body: TransferRequest): Promise<TransferResponse> {
  const response = await apiClient.post<TransferResponse>('/accounts/transfer', body);
  return response.data;
}
