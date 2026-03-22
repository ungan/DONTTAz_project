import { router } from 'expo-router';
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

    // TODO: 백엔드 accountId 반환 fix 후 제거
    if (!useBankAccountStore.getState().accountId) {
      const fallbackId = process.env.EXPO_PUBLIC_ACCOUNT_ID
        ? Number(process.env.EXPO_PUBLIC_ACCOUNT_ID)
        : null;
      if (fallbackId) {
        useBankAccountStore.getState().setAccount(fallbackId, 'DONTTAZ', 0, null, null);
      }
    }
  }

  router.replace('/(tabs)/home');
}
