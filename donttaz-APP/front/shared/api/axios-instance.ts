import axios from 'axios';
import { getStoredAccessToken } from './auth-storage';
import { applyMockInterceptor } from '../mock';

export const apiClient = axios.create({
  baseURL: process.env.EXPO_PUBLIC_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(async (config) => {
  const accessToken = await getStoredAccessToken();

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }

  return config;
});

applyMockInterceptor(apiClient);
