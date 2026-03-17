import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { useFonts } from 'expo-font';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import { GestureHandlerRootView } from 'react-native-gesture-handler';

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
      <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
        <Stack>
          <Stack.Screen name="index" options={{ headerShown: false }} />
          <Stack.Screen name="login" options={{ headerShown: false }} />
          <Stack.Screen name="home" options={{ headerShown: false }} />
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-account" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-amount" options={{ headerShown: false }} />
          <Stack.Screen name="transfer-memo" options={{ headerShown: false }} />
        <Stack.Screen name="transfer-confirm" options={{ headerShown: false }} />
        </Stack>
        <StatusBar style="auto" />
      </ThemeProvider>
    </GestureHandlerRootView>
  );
}
