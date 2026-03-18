import axios from 'axios';
import * as SecureStore from 'expo-secure-store';
import { applyMockInterceptor } from '../mock';

export const apiClient = axios.create({
  baseURL: process.env.EXPO_PUBLIC_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

applyMockInterceptor(apiClient);
