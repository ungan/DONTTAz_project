import { useState, useEffect } from 'react';
import type { Wishlist, WishlistRequest } from '@/entities/vault';

export function useWishlistForm(editTarget?: Wishlist) {
  const [wishName, setWishName] = useState('');
  const [goalAmount, setGoalAmount] = useState('');
  const [targetDate, setTargetDate] = useState(new Date());

  useEffect(() => {
    if (editTarget) {
      setWishName(editTarget.wishName);
      setGoalAmount(String(editTarget.goalAmount));
      setTargetDate(new Date(editTarget.targetDate));
    }
  }, [editTarget]);

  const isValid = wishName.trim().length > 0 && Number(goalAmount) > 0;

  const getRequestBody = (): WishlistRequest => ({
    wishName: wishName.trim(),
    goalAmount: Number(goalAmount),
    targetDate: targetDate.toISOString().split('T')[0],
  });

  return {
    wishName, setWishName,
    goalAmount, setGoalAmount,
    targetDate, setTargetDate,
    isValid,
    getRequestBody,
  };
}
