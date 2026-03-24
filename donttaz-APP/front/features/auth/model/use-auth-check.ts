import { useEffect, useState } from 'react';
import { getStoredAccessToken } from '@/shared/api/auth-storage';

export function useAuthCheck(): 'loading' | 'authenticated' | 'unauthenticated' {
  const [status, setStatus] = useState<'loading' | 'authenticated' | 'unauthenticated'>(
    'loading'
  );

  useEffect(() => {
    const check = async () => {
      const [accessToken] = await Promise.all([
        getStoredAccessToken(),
        new Promise(resolve => setTimeout(resolve, 800)),
      ]);

      const nextStatus = accessToken ? 'authenticated' : 'unauthenticated';

      setStatus(nextStatus);
    };

    void check();
  }, []);

  return status;
}
