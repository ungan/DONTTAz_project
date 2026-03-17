import { useState } from 'react';
import type { TransferFormValues } from '../types/transfer';

export function useTransferForm() {
  const [form, setForm] = useState<TransferFormValues>({
    bankName: '',
    accountNumber: '',
  });

  const setBank = (bank: string) =>
    setForm((prev) => ({ ...prev, bankName: bank as TransferFormValues['bankName'] }));

  const setAccountNumber = (accountNumber: string) =>
    setForm((prev) => ({ ...prev, accountNumber }));

  const isValid = !!form.bankName && form.accountNumber.trim().length > 0;

  return { form, setBank, setAccountNumber, isValid };
}
