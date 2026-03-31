import { apiClient } from '@/shared/api';
import { MonthlyReportParams, MonthlyReportResponse } from '../types/report';

export async function getMonthlyReport(params: MonthlyReportParams): Promise<MonthlyReportResponse> {
  const { data } = await apiClient.get<MonthlyReportResponse>('/ai/reports', {
    params: {
      yearMonth: params.yearMonth,
    },
  });
  return data;
}
