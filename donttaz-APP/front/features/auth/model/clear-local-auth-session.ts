import { useBankAccountStore } from '@/entities/bank-account';
import { useSessionStore } from '@/entities/session';
import { clearStoredAuthTokens, clearStoredDevicePin } from '@/shared/api/auth-storage';
import { useAuthRuntimeStore } from './auth-runtime-store';

export async function clearLocalAuthSession(options?: { keepDevicePin?: boolean }) {
  await clearStoredAuthTokens();

  if (!options?.keepDevicePin) {
    await clearStoredDevicePin();
  }

  useSessionStore.getState().clearSession();
  useBankAccountStore.getState().clearAccount();
  useAuthRuntimeStore.getState().reset();
}
