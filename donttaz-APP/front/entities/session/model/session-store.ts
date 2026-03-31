import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

interface SessionState {
  userId: number | null;
  name: string | null;
  setSession: (userId: number, name: string) => void;
  clearSession: () => void;
}

export const useSessionStore = create<SessionState>()(
  persist(
    (set) => ({
      userId: null,
      name: null,
      setSession: (userId, name) => set({ userId, name }),
      clearSession: () => set({ userId: null, name: null }),
    }),
    {
      name: 'session-store',
      storage: createJSONStorage(() => AsyncStorage),
    }
  )
);
