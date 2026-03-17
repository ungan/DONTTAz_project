import { useState } from 'react';
import { useRouter } from 'expo-router';
import { useBankAccountStore } from '@/entities/bank-account';
import { transferApi } from '../api/transfer';
import { useTransferStore } from './transfer-store';
import type { TransferResult } from '../types/transfer';

export function useTransferSubmit() {
  const router = useRouter();
  const { accountId } = useBankAccountStore();
  const { depositBankCode, depositAccountNumber, amount, withdrawalMemo, depositMemo, setPinToken, clear } =
    useTransferStore();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const submit = async (pin: string) => {
    setPinToken(pin);
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const response = await transferApi({
        withdrawalAccountId: accountId!,
        depositBankCode,
        depositAccountNumber,
        amount,
        withdrawalMemo,
        depositMemo,
        pinToken: pin,
      });

      const result: TransferResult = response.data;
      clear();

      router.push({
        pathname: '/transfer-confirm',
        params: {
          depositAccountName: result.depositAccountName,
          depositBankName: result.depositBankName,
          depositAccountNumber,
          amount: String(result.amount),
          fee: String(result.fee),
          withdrawalBalance: String(result.withdrawalBalance),
        },
      });
    } catch {
      setErrorMessage('송금 처리 중 문제가 발생했습니다.\n잠시 후 다시 시도해주세요.');
    } finally {
      setIsLoading(false);
    }
  };

  const clearError = () => setErrorMessage(null);

  return { submit, isLoading, errorMessage, clearError };
}
