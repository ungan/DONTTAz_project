import { useEffect, useState } from 'react';
import { getMyAccountsApi } from '../api/get-my-accounts';
import type { AccountSummary } from '../types/account';

export function useMyAccounts() {
  const [accounts, setAccounts] = useState<AccountSummary[]>([]);
  const [totalBalance, setTotalBalance] = useState(0);
  const [isPending, setIsPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setIsPending(true);
    setError(null);

    getMyAccountsApi()
      .then((response) => {
        setAccounts(response.data.accountList);
        setTotalBalance(response.data.totalBalance);
      })
      .catch(() => {
        setAccounts([]);
        setTotalBalance(0);
        setError('내 계좌를 불러오지 못했습니다.');
      })
      .finally(() => setIsPending(false));
  }, []);

  return { accounts, totalBalance, isPending, error };
}
