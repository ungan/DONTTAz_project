import { useEffect, useState } from 'react';
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
  const requestParams = {
    page: 0,
    size: 3,
    ...params,
  };

  useEffect(() => {
    if (!accountId) {
      setTransactions([]);
      setError(null);
      setIsPending(false);
      return;
    }

    setIsPending(true);
    setError(null);

    getPaymentsApi({ accountId, ...requestParams })
      .then((res) => setTransactions(res.data.transactions))
      .catch(() => {
        setTransactions([]);
        setError('거래내역을 불러오지 못했습니다.');
      })
      .finally(() => setIsPending(false));
  }, [accountId, requestParams.endDate, requestParams.page, requestParams.size, requestParams.startDate, requestParams.type,]);

  return { transactions, isPending, error };
}
