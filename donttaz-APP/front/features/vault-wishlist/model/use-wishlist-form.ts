import { useState } from 'react';

export function useWishlistForm() {
  const [wishName, setWishName] = useState('');
  const [goalAmount, setGoalAmount] = useState('');

  const isValid = wishName.trim().length > 0 && Number(goalAmount) > 0;

  return {
    wishName, setWishName,
    goalAmount, setGoalAmount,
    isValid,
  };
}
