import React from 'react';
import { ScrollView, View } from 'react-native';
import { WelcomingAlert } from '@/widgets/welcoming-alert';
import { HeroBanner } from '@/widgets/hero-banner';
import { FavoriteMenu } from '@/widgets/favorite-menu';
import { PaymentHistory } from '@/widgets/payment-history';
import { useSessionStore } from '@/entities/session';
import { styles } from './home-page.styles';

export function HomePage() {
  const { name } = useSessionStore();

  return (
    <ScrollView style={styles.scrollView} contentContainerStyle={styles.container} showsVerticalScrollIndicator={false}>
      <View style={styles.whiteCircle} />
      <View style={styles.topSection}>
        <WelcomingAlert userName={name ?? '사용자'} />
        <HeroBanner />
      </View>
      <View style={styles.widgetsSection}>
        <FavoriteMenu />
        <PaymentHistory />
      </View>
    </ScrollView>
  );
}
