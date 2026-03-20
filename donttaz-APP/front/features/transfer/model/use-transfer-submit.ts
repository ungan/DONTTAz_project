import { useState } from 'react';
import { isAxiosError } from 'axios';
import { useRouter } from 'expo-router';
import { useBankAccountStore } from '@/entities/bank-account';
import { transferApi } from '../api/transfer';
import { useTransferStore } from './transfer-store';
import type { TransferResult } from '../types/transfer';

function sanitizeAccountNumber(accountNumber: string | null | undefined) {
  return accountNumber?.replace(/[^0-9]/g, '') ?? '';
}

function isUsableAccountNumber(accountNumber: string | null | undefined) {
  if (!accountNumber || accountNumber.includes('*')) {
    return false;
  }

  return /^[0-9]{10,20}$/.test(sanitizeAccountNumber(accountNumber));
}

export function useTransferSubmit() {
  const router = useRouter();
  const { accountNo: fallbackWithdrawalAccountNo } = useBankAccountStore();
  const {
    bankName,
    depositAccountNumber,
    amount,
    withdrawalMemo,
    depositMemo,
    withdrawalAccountNo: selectedWithdrawalAccountNo,
    clear,
  } = useTransferStore();

  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const submit = async (pin: string) => {
    setIsLoading(true);
    setErrorMessage(null);

    const effectiveWithdrawalAccountNo =
      selectedWithdrawalAccountNo || fallbackWithdrawalAccountNo;
    const sanitizedDepositAccountNumber = sanitizeAccountNumber(depositAccountNumber);
    const sanitizedWithdrawalAccountNumber = sanitizeAccountNumber(effectiveWithdrawalAccountNo);

    if (!isUsableAccountNumber(effectiveWithdrawalAccountNo)) {
      setErrorMessage('출금 계좌번호를 확인할 수 없습니다. 계좌를 다시 불러온 뒤 시도해 주세요.');
      setIsLoading(false);
      return;
    }

    if (!sanitizedDepositAccountNumber) {
      setErrorMessage('입금 계좌번호를 확인한 뒤 다시 시도해 주세요.');
      setIsLoading(false);
      return;
    }

    try {
      const response = await transferApi({
        depositAccountNo: sanitizedDepositAccountNumber,
        depositTransactionSummary: depositMemo.trim() || '입금(이체)',
        transactionBalance: String(amount),
        withdrawalAccountNo: sanitizedWithdrawalAccountNumber,
        withdrawalTransactionSummary: withdrawalMemo.trim() || '출금(이체)',
      });

      const result: TransferResult = response.data;
      clear();

      router.push({
        pathname: '/transfer-confirm',
        params: {
          depositBankName: bankName,
          depositAccountNumber: sanitizedDepositAccountNumber,
          amount: String(amount),
          depositMemo,
          transactionUniqueNo: result.transactions[0]?.transactionUniqueNo ?? '',
          pinLastDigit: pin.slice(-1),
        },
      });
    } catch (error) {
      setErrorMessage(
        isAxiosError(error)
          ? error.response?.data?.message ?? '이체 처리 중 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요.'
          : '이체 처리 중 문제가 발생했습니다.\n잠시 후 다시 시도해 주세요.'
      );
    } finally {
      setIsLoading(false);
    }
  };

  const clearError = () => setErrorMessage(null);

  return { submit, isLoading, errorMessage, clearError };
}
