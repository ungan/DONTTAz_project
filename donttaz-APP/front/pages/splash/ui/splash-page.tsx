import { useEffect } from 'react';
import { Image, Text, View } from 'react-native';
import { router } from 'expo-router';

import { useAuthCheck } from '@/features/auth';
import { styles } from './splash-page.styles';

export function SplashPage() {
  const authStatus = useAuthCheck();

  useEffect(() => {
    if (authStatus === 'authenticated') {
      router.replace('/home');
    } else if (authStatus === 'unauthenticated') {
      router.replace('/login');
    }
  }, [authStatus]);

  return (
    <View style={styles.container}>
      <View style={styles.contentWrapper}>
        <Image
          source={require('@/shared/assets/mascots/donttaz-default.png')}
          style={styles.mascot}
        />
        <Text style={styles.title}>DONTTAz</Text>
      </View>
    </View>
  );
}
