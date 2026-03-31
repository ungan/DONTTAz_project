import AsyncStorage from '@react-native-async-storage/async-storage';

const KEY = 'transfer:recent_accounts';
const MAX = 5;

export interface RecentAccount {
  bankName: string;
  accountNumber: string;
}

export async function loadRecentAccounts(): Promise<RecentAccount[]> {
  try {
    const raw = await AsyncStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

export async function saveRecentAccount(account: RecentAccount): Promise<void> {
  try {
    const existing = await loadRecentAccounts();
    const filtered = existing.filter(
      (a) => !(a.bankName === account.bankName && a.accountNumber === account.accountNumber)
    );
    const updated = [account, ...filtered].slice(0, MAX);
    await AsyncStorage.setItem(KEY, JSON.stringify(updated));
  } catch {
  }
}
