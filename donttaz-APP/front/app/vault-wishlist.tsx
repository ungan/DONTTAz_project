import { Stack } from 'expo-router';
import { VaultWishlistPage } from '@/pages/vault-wishlist';

export default function VaultWishlistRoute() {
  return (
    <>
      <Stack.Screen options={{ headerShown: false }} />
      <VaultWishlistPage />
    </>
  );
}
