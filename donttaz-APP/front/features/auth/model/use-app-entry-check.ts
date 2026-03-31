import { useEffect, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { clearStoredAuthTokens, clearStoredDevicePin, getStoredAccessToken, getStoredDevicePin, getStoredRefreshToken } from '@/shared/api/auth-storage';

const APP_INSTALLED_KEY = 'app_installed';

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
      const isInstalled = await AsyncStorage.getItem(APP_INSTALLED_KEY);
      if (!isInstalled) {
        await clearStoredAuthTokens();
        await clearStoredDevicePin();
        await AsyncStorage.setItem(APP_INSTALLED_KEY, 'true');
      }

      const [refreshToken, storedPin, accessToken] = await Promise.all([
        getStoredRefreshToken(),
        getStoredDevicePin(),
        getStoredAccessToken(),
        new Promise(resolve => setTimeout(resolve, 800)),
      ]);

      const hasRefreshToken = !!refreshToken;
      const hasDevicePin = !!storedPin;
      const hasAccessToken = !!accessToken;

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
