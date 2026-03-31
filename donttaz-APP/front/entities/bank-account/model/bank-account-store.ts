import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface BankAccountState {
  accountId: number | null;
  bankName: string | null;
  balance: number | null;
  accountNo: string | null;
  accountName: string | null;
  setAccount: (
    accountId: number | null,
    bankName: string,
    balance: number,
    accountNo?: string | null,
    accountName?: string | null
  ) => void;
  clearAccount: () => void;
}

export const useBankAccountStore = create<BankAccountState>()(
  persist(
    (set) => ({
      accountId: null,
      bankName: null,
      balance: null,
      accountNo: null,
      accountName: null,
      setAccount: (accountId, bankName, balance, accountNo = null, accountName = null) =>
        set({ accountId, bankName, balance, accountNo, accountName }),
      clearAccount: () =>
        set({
          accountId: null,
          bankName: null,
          balance: null,
          accountNo: null,
          accountName: null,
        }),
    }),
    {
      name: 'bank-account-store',
      storage: createJSONStorage(() => AsyncStorage),
    }
  )
);
