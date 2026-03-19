import { apiClient } from '@/shared/api';
import type { LoginResponse } from '../types/login';
import type { RefreshRequest } from '../types/refresh';

export async function refreshApi(body: RefreshRequest): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/auth/refresh', body);
  return response.data;
}
