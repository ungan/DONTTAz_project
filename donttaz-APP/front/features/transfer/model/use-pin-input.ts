import { useState } from 'react';

const PIN_LENGTH = 6;

type KeyValue = '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '0' | '00' | 'X';

interface UsePinInputOptions {
  onComplete: (pin: string) => void;
}

export function usePinInput({ onComplete }: UsePinInputOptions) {
  const [pin, setPin] = useState('');

  const handleKeyPress = (key: KeyValue) => {
    if (key === 'X') {
      setPin((prev) => prev.slice(0, -1));
      return;
    }

    if (pin.length >= PIN_LENGTH) return;
    const next = pin + key;
    setPin(next);
    if (next.length === PIN_LENGTH) {
      onComplete(next);
    }
  };

  const reset = () => setPin('');

  return { filledCount: pin.length, handleKeyPress, reset };
}
