import { Stack } from 'expo-router';
import { VaultMemorandumPage } from '@/pages/vault-memorandum';

export default function VaultMemorandumRoute() {
  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <VaultMemorandumPage />
    </>
  );
}
