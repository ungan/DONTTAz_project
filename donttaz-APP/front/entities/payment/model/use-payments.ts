import { useState, useEffect } from 'react';
import { useBankAccountStore } from '@/entities/bank-account';
import { getPaymentsApi } from '../api/get-payments';
import type { Transaction, TransactionType } from '../types/payment';

interface UsePaymentsParams {
  startDate?: string;
  endDate?: string;
  type?: TransactionType;
  page?: number;
  size?: number;
}

export function usePayments(params?: UsePaymentsParams) {
  const { accountId } = useBankAccountStore();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!accountId) return;

    setIsPending(true);
    setError(null);

    getPaymentsApi({ accountId, ...params })
      .then((res) => setTransactions(res.data.transactions))
      .catch(() => setError('거래내역을 불러오지 못했습니다.'))
      .finally(() => setIsPending(false));
  }, [accountId]);

  return { transactions, isPending, error };
}
