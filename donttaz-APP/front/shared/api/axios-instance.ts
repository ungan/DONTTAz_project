import axios from 'axios';
import { applyMockInterceptor } from '../mock';

export const apiClient = axios.create({
  baseURL: process.env.EXPO_PUBLIC_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

applyMockInterceptor(apiClient); // 실제 API 연결 시 이 줄 제거
