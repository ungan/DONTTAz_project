import { apiClient } from '@/shared/api';
import type { LogoutRequest, LogoutResponse } from '../types/logout';

export async function logoutApi(body: LogoutRequest): Promise<LogoutResponse> {
  const response = await apiClient.post<LogoutResponse>('/auth/logout', body);
  return response.data;
}
