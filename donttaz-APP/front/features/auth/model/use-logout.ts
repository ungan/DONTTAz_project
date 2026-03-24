import { useState } from 'react';
import { isAxiosError } from 'axios';
import { router } from 'expo-router';
import {
  getStoredAccessToken,
  getStoredRefreshToken,
  storeAuthTokens,
} from '@/shared/api/auth-storage';
import { logoutApi } from '../api/logout';
import { refreshApi } from '../api/refresh';
import { clearLocalAuthSession } from './clear-local-auth-session';

function getLogoutErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? '로그아웃 처리 중 문제가 발생했습니다.';
  }

  return '로그아웃 처리 중 문제가 발생했습니다.';
}

export function useLogout() {
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const logout = async () => {
    setIsPending(true);
    setError(null);

    try {
      const [accessToken, refreshToken] = await Promise.all([
        getStoredAccessToken(),
        getStoredRefreshToken(),
      ]);

      if (!accessToken) {
        if (refreshToken) {
          const refreshResponse = await refreshApi({ refreshToken });
          const refreshedAccessToken = refreshResponse.data.accessToken;
          const refreshedRefreshToken = refreshResponse.data.refreshToken;

          await storeAuthTokens(refreshedAccessToken, refreshedRefreshToken);
          await logoutApi({ refreshToken: refreshedRefreshToken });
          await clearLocalAuthSession();
          router.replace('/login');
          return true;
        }

        await clearLocalAuthSession();
        router.replace('/login');
        return true;
      }

      if (refreshToken) {
        await logoutApi({ refreshToken });
      }

      await clearLocalAuthSession();
      router.replace('/login');
      return true;
    } catch (error) {
      try {
        await clearLocalAuthSession();
        router.replace('/login');
        return true;
      } catch {
        setError(getLogoutErrorMessage(error));
        return false;
      }
    } finally {
      setIsPending(false);
    }
  };

  const resetError = () => setError(null);

  return {
    logout,
    isPending,
    error,
    resetError,
  };
}
