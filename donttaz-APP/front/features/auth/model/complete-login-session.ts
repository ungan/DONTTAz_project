import { router } from 'expo-router';
import { getPrimaryAccountApi } from '@/entities/account';
import { useSessionStore } from '@/entities/session';
import { useBankAccountStore } from '@/entities/bank-account';
import { syncPushTokenWithServer } from '@/features/push';
import { storeAuthTokens } from '@/shared/api/auth-storage';
import type { LoginResponseData } from '../types/login';
import { useAuthRuntimeStore } from './auth-runtime-store';

function parseBalance(balance: string | number | null | undefined) {
  const numericBalance = typeof balance === 'number' ? balance : Number(balance ?? 0);
  return Number.isFinite(numericBalance) ? numericBalance : 0;
}

function resolveFallbackAccountId() {
  return process.env.EXPO_PUBLIC_ACCOUNT_ID ? Number(process.env.EXPO_PUBLIC_ACCOUNT_ID) : null;
}

async function syncPrimaryAccountFromApi() {
  const account = await getPrimaryAccountApi();

  const resolvedAccountId = account.accountId ?? resolveFallbackAccountId();
  if (!resolvedAccountId) {
    return false;
  }

  useBankAccountStore.getState().setAccount(
    resolvedAccountId,
    account.bankName,
    parseBalance(account.accountBalance),
    account.accountNo,
    account.accountName
  );

  return true;
}

export async function completeLoginSession(data: LoginResponseData) {
  await storeAuthTokens(data.accessToken, data.refreshToken);

  useSessionStore.getState().setSession(data.userId, data.name);
  useAuthRuntimeStore.getState().setPinVerified(true);

  try {
    const synced = await syncPrimaryAccountFromApi();

    if (!synced && !useBankAccountStore.getState().accountId) {
      const fallbackId = resolveFallbackAccountId();
      if (fallbackId) {
        useBankAccountStore.getState().setAccount(fallbackId, 'DONTTAZ', 0, null, null);
      }
    }
  } catch {
    if (!useBankAccountStore.getState().accountId) {
      const fallbackId = resolveFallbackAccountId();
      if (fallbackId) {
        useBankAccountStore.getState().setAccount(fallbackId, 'DONTTAZ', 0, null, null);
      }
    }
  }

  void syncPushTokenWithServer();

  router.replace('/(tabs)/home');
}
