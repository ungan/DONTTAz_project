import { apiClient } from '@/shared/api';
import type { BurndownResponse } from '../model/types';

export async function getBurndown(yearMonth: string): Promise<BurndownResponse> {
  const { data } = await apiClient.get<BurndownResponse>('/control/burndown', {
    headers: { yearMonth },
  });
  return data;
}
