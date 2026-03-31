import { apiClient } from '@/shared/api';
import type { BurndownResponse } from '../model/types';

export async function getBurndown(yearMonth: string, _source = 'unknown'): Promise<BurndownResponse> {
  const { data } = await apiClient.get<BurndownResponse>('/control/burndown', {
    params: { yearMonth },
  });

  return data;
}
