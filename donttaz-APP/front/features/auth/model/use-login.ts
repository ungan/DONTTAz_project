import { useState } from 'react';
import { isAxiosError } from 'axios';
import { loginApi } from '../api/login';
import { completeLoginSession } from './complete-login-session';

function getLoginErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    if (error.response?.status === 401) {
      return error.response.data?.message ?? '이메일 또는 비밀번호가 올바르지 않습니다.';
    }

    return error.response?.data?.message ?? '로그인 처리 중 문제가 발생했습니다.';
  }

  return '로그인 처리 중 문제가 발생했습니다.';
}

export function useLogin() {
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const login = async (loginId: string, password: string) => {
    if (__DEV__) {
      console.log('[LoginDebug][useLogin] start', {
        loginId,
        hasPassword: password.length > 0,
        baseUrl: process.env.EXPO_PUBLIC_BASE_URL,
        isMock: process.env.EXPO_PUBLIC_IS_MOCK,
      });
    }

    setIsPending(true);
    setError(null);

    try {
      if (__DEV__) {
        console.log('[LoginDebug][useLogin] request:/auth/login');
      }

      const res = await loginApi({ loginId, password });

      if (__DEV__) {
        console.log('[LoginDebug][useLogin] success', {
          hasAccessToken: !!res.data?.accessToken,
          hasRefreshToken: !!res.data?.refreshToken,
          userId: res.data?.userId,
        });
      }

      await completeLoginSession(res.data);
    } catch (error) {
      if (__DEV__) {
        console.log('[LoginDebug][useLogin] error', {
          error,
          message: error instanceof Error ? error.message : String(error),
        });
      }

      setError(getLoginErrorMessage(error));
    } finally {
      setIsPending(false);
    }
  };

  return { login, isPending, error };
}
