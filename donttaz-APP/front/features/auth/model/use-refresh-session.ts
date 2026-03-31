import { useState } from 'react';
import { isAxiosError } from 'axios';
import { completeLoginSession } from './complete-login-session';
import { refreshApi } from '../api/refresh';

function getRefreshErrorMessage(error: unknown) {
  if (isAxiosError(error)) {
    return error.response?.data?.message ?? '세션을 복구하지 못했습니다.';
  }

  return '세션을 복구하지 못했습니다.';
}

export function useRefreshSession() {
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshSession = async (refreshToken: string) => {
    setIsPending(true);
    setError(null);

    try {
      const response = await refreshApi({ refreshToken });
      await completeLoginSession(response.data);
      return true;
    } catch (error) {
      setError(getRefreshErrorMessage(error));
      return false;
    } finally {
      setIsPending(false);
    }
  };

  const resetError = () => setError(null);

  return {
    refreshSession,
    isPending,
    error,
    resetError,
  };
}
