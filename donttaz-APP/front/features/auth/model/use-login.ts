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
    setIsPending(true);
    setError(null);

    try {
      const res = await loginApi({ loginId, password });
      await completeLoginSession(res.data);
    } catch (error) {
      setError(getLoginErrorMessage(error));
    } finally {
      setIsPending(false);
    }
  };

  return { login, isPending, error };
}
