import { create } from 'zustand';
import type { Wishlist } from '../types/vault';

interface WishlistState {
  wishlists: Wishlist[];
  setWishlists: (wishlists: Wishlist[]) => void;
}

export const useWishlistStore = create<WishlistState>((set) => ({
  wishlists: [],
  setWishlists: (wishlists) => set({ wishlists }),
}));
