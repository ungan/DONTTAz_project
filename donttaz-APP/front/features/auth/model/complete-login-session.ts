import { router } from 'expo-router';
import { useSessionStore } from '@/entities/session';
import { useBankAccountStore } from '@/entities/bank-account';
import { storeAuthTokens } from '@/shared/api/auth-storage';
import mockBankAccount from '@/shared/mock/data/bank-account.json';
import type { LoginResponseData } from '../types/login';
import { useAuthRuntimeStore } from './auth-runtime-store';

export async function completeLoginSession(data: LoginResponseData) {
  await storeAuthTokens(data.accessToken, data.refreshToken);

  useSessionStore.getState().setSession(data.userId, data.name);
  useAuthRuntimeStore.getState().setPinVerified(true);

  // TODO: 계좌 조회 API 연동 이후 mock 주입 제거
  useBankAccountStore.getState().setAccount(
    mockBankAccount.data.accountId,
    mockBankAccount.data.bankName,
    mockBankAccount.data.balance
  );

  router.replace('/(tabs)/home');
}
