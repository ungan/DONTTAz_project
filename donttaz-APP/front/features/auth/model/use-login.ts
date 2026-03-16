import { useState } from 'react';
import { router } from 'expo-router';
import * as SecureStore from 'expo-secure-store';
import { loginApi } from '../api/login';
import { useSessionStore } from '@/entities/session';
import { useBankAccountStore } from '@/entities/bank-account';
import mockBankAccount from '@/shared/mock/data/bank-account.json';

export function useLogin() {
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { setSession } = useSessionStore();
  const { setAccount } = useBankAccountStore();

  const login = async (loginId: string, password: string) => {
    setIsPending(true);
    setError(null);
    try {
      const res = await loginApi({ loginId, password });
      await SecureStore.setItemAsync('accessToken', res.data.accessToken);
      await SecureStore.setItemAsync('refreshToken', res.data.refreshToken);
      setSession(res.data.userId, res.data.name);
      // TODO: 계좌 조회 API 생기면 교체
      setAccount(
        mockBankAccount.data.accountId,
        mockBankAccount.data.bankName,
        mockBankAccount.data.balance
      );
      router.replace('/home');
    } catch {
      setError('로그인에 실패했습니다.');
    } finally {
      setIsPending(false);
    }
  };

  return { login, isPending, error };
}
