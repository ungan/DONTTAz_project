import React from 'react';
import { ScrollView, View } from 'react-native';
import { useRouter } from 'expo-router';
import { WelcomingAlert } from '@/widgets/welcoming-alert';
import { HeroBanner } from '@/widgets/hero-banner';
import { FavoriteMenu } from '@/widgets/favorite-menu';
import { PaymentHistory } from '@/widgets/payment-history';
import { useSessionStore } from '@/entities/session';
import { styles } from './home-page.styles';

export function HomePage() {
  const { name } = useSessionStore();
  const router = useRouter();

  const handleFavoriteMenuPress = (id: string) => {
    if (id === '1') {
      router.push('/transfer-account');
    }
    if (id === '2') {
      router.push('/report');
    }
    if (id === '3') {
      router.push('/(tabs)/plan');
    }
    if (id === '4') {
      router.push('/(tabs)/vault');
    }
  };

  return (
    <ScrollView style={styles.scrollView} contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.whiteCircle} />
      <View style={styles.topSection}>
        <WelcomingAlert userName={name ?? '사용자'} />
        <HeroBanner />
      </View>
      <View style={styles.widgetsSection}>
        <FavoriteMenu onItemPress={handleFavoriteMenuPress} />
        <PaymentHistory onViewAllPress={() => router.push('/payment-history')} />
      </View>
    </ScrollView>
  );
}
