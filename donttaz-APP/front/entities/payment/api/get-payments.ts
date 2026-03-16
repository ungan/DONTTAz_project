import { apiClient } from '@/shared/api';
import type { GetPaymentsRequest, GetPaymentsResponse } from '../types/payment';

export async function getPaymentsApi({
  accountId,
  startDate,
  endDate,
  type,
  page = 0,
  size = 20,
}: GetPaymentsRequest): Promise<GetPaymentsResponse> {
  const response = await apiClient.get<GetPaymentsResponse>(
    `/accounts/${accountId}/transactions`,
    {
      params: { startDate, endDate, type, page, size },
    }
  );
  return response.data;
}
