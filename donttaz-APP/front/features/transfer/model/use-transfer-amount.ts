import { useState } from 'react';

const MAX_AMOUNT = 100_000_000;

type KeyValue = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '0' | '00' | 'X';

export function useTransferAmount() {
  const [amount, setAmount] = useState(0);

  const handleKeyPress = (key: KeyValue) => {
    setAmount((prev) => {
      if (key === 'X') return Math.floor(prev / 10);
      if (key === '00') {
        const next = prev * 100;
        return next > MAX_AMOUNT ? prev : next;
      }
      const next = prev * 10 + Number(key);
      return next > MAX_AMOUNT ? prev : next;
    });
  };

  const handleQuickAdd = (value: number) => {
    setAmount((prev) => Math.min(prev + value, MAX_AMOUNT));
  };

  const formattedAmount = amount.toLocaleString('ko-KR');
  const isValid = amount >= 1;

  return { amount, formattedAmount, isValid, handleKeyPress, handleQuickAdd };
}
