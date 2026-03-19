import { useEffect, useState } from 'react';
import { getStoredAccessToken, getStoredDevicePin, getStoredRefreshToken } from '@/shared/api/auth-storage';

export function useAppEntryCheck():
  | 'loading'
  | 'authenticated'
  | 'pin-required'
  | 'unauthenticated' {
  const [status, setStatus] = useState<
    'loading' | 'authenticated' | 'pin-required' | 'unauthenticated'
  >('loading');

  useEffect(() => {
    const check = async () => {
      const [refreshToken, storedPin, accessToken] = await Promise.all([
        getStoredRefreshToken(),
        getStoredDevicePin(),
        getStoredAccessToken(),
        new Promise(resolve => setTimeout(resolve, 800)),
      ]);

      if (refreshToken && storedPin) {
        setStatus('pin-required');
        return;
      }

      if (accessToken) {
        setStatus('authenticated');
        return;
      }

      setStatus('unauthenticated');
    };

    void check();
  }, []);

  return status;
}
