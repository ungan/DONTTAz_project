import { create } from 'zustand';

interface AuthRuntimeState {
  isPinVerified: boolean;
  setPinVerified: (value: boolean) => void;
  reset: () => void;
}

export const useAuthRuntimeStore = create<AuthRuntimeState>((set) => ({
  isPinVerified: false,
  setPinVerified: (isPinVerified) => set({ isPinVerified }),
  reset: () => set({ isPinVerified: false }),
}));
