import { router } from 'expo-router';
import { getMyAccountsApi } from '@/entities/account';
import { useSessionStore } from '@/entities/session';
import { useBankAccountStore } from '@/entities/bank-account';
import { storeAuthTokens } from '@/shared/api/auth-storage';
import mockBankAccount from '@/shared/mock/data/bank-account.json';
import type { LoginResponseData } from '../types/login';
import { useAuthRuntimeStore } from './auth-runtime-store';

function isLegacyMockAccount(accountId: number | null, accountNo: string | null) {
  return (
    accountId === mockBankAccount.data.accountId || accountNo === mockBankAccount.data.accountNo
  );
}

function parseBalance(balance: string | number | null | undefined) {
  const numericBalance = typeof balance === 'number' ? balance : Number(balance ?? 0);
  return Number.isFinite(numericBalance) ? numericBalance : 0;
}

function resolveFallbackAccountId() {
  return process.env.EXPO_PUBLIC_ACCOUNT_ID ? Number(process.env.EXPO_PUBLIC_ACCOUNT_ID) : null;
}

async function syncPrimaryAccountFromApi() {
  const response = await getMyAccountsApi();
  const primaryAccount = response.data.accountList.find((account) => account.isPrimary) ?? response.data.accountList[0];

  if (!primaryAccount) {
    return false;
  }

  const resolvedAccountId = primaryAccount.accountId ?? resolveFallbackAccountId();
  if (!resolvedAccountId) {
    return false;
  }

  useBankAccountStore.getState().setAccount(
    resolvedAccountId,
    primaryAccount.bankName,
    parseBalance(primaryAccount.accountBalance),
    primaryAccount.accountNo,
    primaryAccount.accountName
  );

  return true;
}

export async function completeLoginSession(data: LoginResponseData) {
  await storeAuthTokens(data.accessToken, data.refreshToken);

  useSessionStore.getState().setSession(data.userId, data.name);
  useAuthRuntimeStore.getState().setPinVerified(true);

  if (process.env.EXPO_PUBLIC_IS_MOCK === 'true') {
    useBankAccountStore.getState().setAccount(
      mockBankAccount.data.accountId,
      mockBankAccount.data.bankName,
      mockBankAccount.data.balance,
      mockBankAccount.data.accountNo,
      mockBankAccount.data.accountName
    );
  } else {
    const currentAccount = useBankAccountStore.getState();

    if (isLegacyMockAccount(currentAccount.accountId, currentAccount.accountNo)) {
      currentAccount.clearAccount();
    }

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
  }

  router.replace('/(tabs)/home');
}
