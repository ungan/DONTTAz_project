import { create } from 'zustand';

interface TransferState {
  withdrawalBankName: string;
  withdrawalAccountName: string;
  withdrawalAccountLabel: string;
  withdrawalAccountNo: string;
  bankName: string;
  depositAccountNumber: string;
  amount: number;
  withdrawalMemo: string;
  depositMemo: string;
  setWithdrawalAccount: (
    withdrawalBankName: string,
    withdrawalAccountName: string,
    withdrawalAccountLabel: string,
    withdrawalAccountNo: string
  ) => void;
  setAccountInfo: (bankName: string, depositAccountNumber: string) => void;
  setAmount: (amount: number) => void;
  setMemo: (withdrawalMemo: string, depositMemo: string) => void;
  clear: () => void;
}

const initialState = {
  withdrawalBankName: '',
  withdrawalAccountName: '',
  withdrawalAccountLabel: '',
  withdrawalAccountNo: '',
  bankName: '',
  depositAccountNumber: '',
  amount: 0,
  withdrawalMemo: '',
  depositMemo: '',
};

export const useTransferStore = create<TransferState>((set) => ({
  ...initialState,
  setWithdrawalAccount: (
    withdrawalBankName,
    withdrawalAccountName,
    withdrawalAccountLabel,
    withdrawalAccountNo
  ) =>
    set({
      withdrawalBankName,
      withdrawalAccountName,
      withdrawalAccountLabel,
      withdrawalAccountNo,
    }),
  setAccountInfo: (bankName, depositAccountNumber) => set({ bankName, depositAccountNumber }),
  setAmount: (amount) => set({ amount }),
  setMemo: (withdrawalMemo, depositMemo) => set({ withdrawalMemo, depositMemo }),
  clear: () => set(initialState),
}));
