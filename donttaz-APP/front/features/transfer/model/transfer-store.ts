import { create } from 'zustand';

interface TransferState {
  bankName: string;
  depositBankCode: string;
  depositAccountNumber: string;
  amount: number;
  withdrawalMemo: string;
  depositMemo: string;
  pinToken: string;
  setAccountInfo: (bankName: string, depositBankCode: string, depositAccountNumber: string) => void;
  setAmount: (amount: number) => void;
  setMemo: (withdrawalMemo: string, depositMemo: string) => void;
  setPinToken: (pinToken: string) => void;
  clear: () => void;
}

const initialState = {
  bankName: '',
  depositBankCode: '',
  depositAccountNumber: '',
  amount: 0,
  withdrawalMemo: '',
  depositMemo: '',
  pinToken: '',
};

export const useTransferStore = create<TransferState>((set) => ({
  ...initialState,
  setAccountInfo: (bankName, depositBankCode, depositAccountNumber) =>
    set({ bankName, depositBankCode, depositAccountNumber }),
  setAmount: (amount) => set({ amount }),
  setMemo: (withdrawalMemo, depositMemo) => set({ withdrawalMemo, depositMemo }),
  setPinToken: (pinToken) => set({ pinToken }),
  clear: () => set(initialState),
}));
