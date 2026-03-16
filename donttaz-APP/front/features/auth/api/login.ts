import { apiClient } from '@/shared/api';
import type { LoginRequest, LoginResponse } from '../types/login';

export async function loginApi(body: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/auth/login', body);
  return response.data;
}
