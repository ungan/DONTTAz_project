import { apiClient } from '@/shared/api';
import { MonthlyReportParams, MonthlyReportResponse } from '../types/report';

export async function getMonthlyReport(params: MonthlyReportParams): Promise<MonthlyReportResponse> {
  const { data } = await apiClient.get<MonthlyReportResponse>('/ai/transactions/categories', {
    params: {
      yearMonth: params.yearMonth,
      page: params.page ?? 0,
      size: params.size ?? 20,
    },
  });
  return data;
}
