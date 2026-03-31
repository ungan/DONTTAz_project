import { apiClient } from '@/shared/api';

interface YearlyReportResponse {
  status: string;
  message: string;
  data: Record<string, Record<string, number>>;
}

export async function getYearlyReport(): Promise<YearlyReportResponse> {
  const { data } = await apiClient.get<YearlyReportResponse>('/ai/transactions/yearly');
  return data;
}
