import React from 'react';
import { ScrollView, View, Alert } from 'react-native';
import { useRouter } from 'expo-router';
import { WelcomingAlert } from '@/widgets/welcoming-alert';
import { HeroBanner } from '@/widgets/hero-banner';
import { FavoriteMenu } from '@/widgets/favorite-menu';
import { PaymentHistory } from '@/widgets/payment-history';
import { useSessionStore } from '@/entities/session';
import { getVaultApi } from '@/entities/vault';
import { styles } from './home-page.styles';

export function HomePage() {
  const { name } = useSessionStore();
  const router = useRouter();

  const handleFavoriteMenuPress = async (id: string) => {
    if (id === '1') {
      router.push('/transfer-account');
    }
    if (id === '2') {
      router.push('/report');
    }
    if (id === '3') {
      try {
        const { vaults } = await getVaultApi();
        const wishlist = vaults.find((v) => v.vaultKind === 'WISHLIST');
        if (!wishlist) {
          Alert.alert(
            '위시금고 필요',
            '각서를 보려면 먼저 위시금고를 만들어야 합니다.',
            [
              { text: '취소', style: 'cancel' },
              { text: '위시금고 만들기', onPress: () => router.push('/(tabs)/vault?tab=wishlist') },
            ]
          );
          return;
        }
        router.push(`/vault-memorandum?readonly=true&vaultId=${wishlist.vaultId}`);
      } catch {
        router.push('/vault-memorandum?readonly=true');
      }
    }
    if (id === '4') {
      try {
        const { vaults } = await getVaultApi();
        const wishlist = vaults.find((v) => v.vaultKind === 'WISHLIST');
        if (!wishlist) {
          Alert.alert(
            '위시금고 필요',
            '수치심 메세지를 설정하려면 먼저 위시금고를 만들어야 합니다.',
            [
              { text: '취소', style: 'cancel' },
              { text: '위시금고 만들기', onPress: () => router.push('/(tabs)/vault?tab=wishlist') },
            ]
          );
          return;
        }
        router.push(`/vault-webhook?vaultId=${wishlist.vaultId}`);
      } catch {
        router.push('/vault-webhook');
      }
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
