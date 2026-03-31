import { useEffect, useState } from 'react';
import { loadRecentAccounts } from './recent-accounts';
import type { RecentAccount } from './recent-accounts';

export function useRecentAccounts() {
  const [accounts, setAccounts] = useState<RecentAccount[]>([]);

  useEffect(() => {
    loadRecentAccounts().then(setAccounts);
  }, []);

  return accounts;
}
