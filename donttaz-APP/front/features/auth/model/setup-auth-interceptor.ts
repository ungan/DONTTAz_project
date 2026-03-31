import axios from 'axios';
import { router } from 'expo-router';
import { apiClient } from '@/shared/api';
import { getStoredRefreshToken, storeAuthTokens } from '@/shared/api/auth-storage';
import { clearLocalAuthSession } from './clear-local-auth-session';

export function setupAuthInterceptor() {
  apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config;

      if (error.response?.status !== 401 || originalRequest._retry) {
        return Promise.reject(error);
      }

      originalRequest._retry = true;

      const refreshToken = await getStoredRefreshToken();
      if (!refreshToken) {
        console.log('[Auth] No refresh token — redirecting to login');
        console.log('[Auth] No refresh token — redirecting to login');
        await clearLocalAuthSession({ keepDevicePin: true });
        router.replace('/login');
        return Promise.reject(error);
      }

      try {
        console.log('[Auth] Access token expired — attempting refresh...');
        const res = await axios.post(
          `${process.env.EXPO_PUBLIC_BASE_URL}/auth/refresh`,
          { refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        );

        const { accessToken, refreshToken: newRefreshToken } = res.data;
        console.log('[Auth] Token refresh success — retrying original request');

        await storeAuthTokens(accessToken, newRefreshToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        console.log('[Auth] Token refresh failed — redirecting to login');
        await clearLocalAuthSession({ keepDevicePin: true });
        router.replace('/login');
        return Promise.reject(refreshError);
      }
    }
  );
}
