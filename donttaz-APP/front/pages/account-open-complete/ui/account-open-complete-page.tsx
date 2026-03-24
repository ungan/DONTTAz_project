import React from 'react';
import { View, Text } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Image } from 'react-native';
import { Button } from '@/shared/ui';
import { useAccountOpenFlow } from '@/features/account-open';
import { styles } from './account-open-complete-page.styles';

export function AccountOpenCompletePage() {
  const insets = useSafeAreaInsets();
  const { createdBankName, createdAccountNumber, createdAccountAlias } = useAccountOpenFlow();

  return (
    <View style={[styles.container, { paddingTop: insets.top + 24 }]}>
      <View style={styles.content}>
        <View style={styles.iconWrap}>
          <Image
            source={require('@/shared/assets/icons/checked.png')}
            style={styles.icon}
            resizeMode="contain"
          />
        </View>

        <View style={styles.heroSection}>
          <Text style={styles.title}>계좌 개설 완료</Text>
          <Text style={styles.description}>새 계좌가 성공적으로 개설되었습니다.</Text>
        </View>

        <View style={styles.summaryCard}>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>상품명</Text>
            <Text style={styles.summaryValue}>{createdBankName || 'DONTTAZ 예금'}</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>계좌번호</Text>
            <Text style={styles.summaryValue}>{createdAccountNumber || '-'}</Text>
          </View>
          <View style={styles.divider} />
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>계좌 별칭</Text>
            <Text style={styles.summaryValue}>{createdAccountAlias || '나의 통장'}</Text>
          </View>
        </View>
      </View>

      <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
        <Button label="홈으로" onPress={() => router.replace('/(tabs)/home' as never)} />
      </View>
    </View>
  );
}
