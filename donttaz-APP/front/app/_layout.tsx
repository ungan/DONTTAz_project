import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { useColorScheme } from '@/shared/hooks/use-color-scheme';

export default function RootLayout() {
  const colorScheme = useColorScheme();
  const [fontsLoaded, fontError] = useFonts({
    Chewy: require('@/shared/assets/fonts/Chewy-Regular.ttf'),
    BalooDa2Bold: require('@/shared/assets/fonts/BalooDa2-Bold.ttf'),
  });

  if (fontError) console.error('Font load error:', fontError);
  if (fontsLoaded) console.log('Fonts loaded successfully');
  if (!fontsLoaded) return null;

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
        <Stack>
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="pin-login" options={{ headerShown: false }} />
          <Stack.Screen name="account-open-terms" options={{ headerShown: false }} />
          <Stack.Screen name="account-open-profile" options={{ headerShown: false }} />
          <Stack.Screen name="account-open-banking" options={{ headerShown: false }} />
          <Stack.Screen name="account-open-verify" options={{ headerShown: false }} />
          <Stack.Screen name="account-open-complete" options={{ headerShown: false }} />
          <Stack.Screen name="signup-start" options={{ headerShown: false }} />
          <Stack.Screen name="signup-account" options={{ headerShown: false }} />
          <Stack.Screen name="signup-pin" options={{ headerShown: false }} />
          <Stack.Screen name="signup-complete" options={{ headerShown: false }} />
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-account" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-amount" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-memo" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-confirm" options={{ headerShown: false }} />
          <Stack.Screen name="report" options={{ headerShown: false }} />
          <Stack.Screen name="report-monthly" options={{ headerShown: false }} />
          <Stack.Screen name="vault-memorandum" options={{ headerShown: false }} />
          <Stack.Screen name="vault-webhook" options={{ headerShown: false }} />
          <Stack.Screen name="vault-deposit" options={{ headerShown: false }} />
          <Stack.Screen name="payment-history" options={{ headerShown: false, animation: 'slide_from_right' }} />
        </Stack>
        <StatusBar style="auto" />
      </ThemeProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}
