import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface BankAccountState {
  accountId: number | null;
  bankName: string | null;
  balance: number | null;
  setAccount: (accountId: number, bankName: string, balance: number) => void;
  clearAccount: () => void;
}

export const useBankAccountStore = create<BankAccountState>()(
  persist(
    (set) => ({
      accountId: null,
      bankName: null,
      balance: null,
      setAccount: (accountId, bankName, balance) =>
        set({ accountId, bankName, balance }),
      clearAccount: () =>
        set({ accountId: null, bankName: null, balance: null }),
    }),
    {
      name: 'bank-account-store',
      storage: createJSONStorage(() => AsyncStorage),
    }
  )
);
