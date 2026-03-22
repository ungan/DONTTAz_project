import { Stack } from 'expo-router';
import { VaultWebhookPage } from '@/pages/vault-webhook';

export default function VaultWebhookRoute() {
  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <VaultWebhookPage />
    </>
  );
}
