import { useState } from 'react';

type KeyValue = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '0' | '00' | 'X';

export function useDepositAmount(maxAmount = 100_000_000) {
  const [amount, setAmount] = useState(0);

  const handleKeyPress = (key: KeyValue) => {
    setAmount((prev) => {
      if (key === 'X') return Math.floor(prev / 10);
      if (key === '00') {
        const next = prev * 100;
        return next > maxAmount ? prev : next;
      }
      const next = prev * 10 + Number(key);
      return next > maxAmount ? prev : next;
    });
  };

  const formattedAmount = amount.toLocaleString('ko-KR');
  const isValid = amount >= 1 && amount <= maxAmount;

  return { amount, formattedAmount, isValid, handleKeyPress };
}
