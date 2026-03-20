import React from 'react';
import { View, Text } from 'react-native';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import { useAccountOpenFlow } from '@/features/account-open';
import { styles } from './account-open-complete-page.styles';

export function AccountOpenCompletePage() {
  const { createdBankName, createdAccountNumber, createdAccountAlias } = useAccountOpenFlow();

  return (
    <View style={styles.container}>
      <PageHeader title="계좌 개설 완료" onBack={() => router.replace('/(tabs)/menu' as never)} />

      <View style={styles.heroSection}>
        <Text style={styles.title}>계좌 개설 완료</Text>
        <Text style={styles.description}>
          계좌 생성이 완료되었습니다. 상품명과 계좌번호는 현재 계좌 생성 API 응답을 기준으로
          표시합니다.
        </Text>
      </View>

      <View style={styles.summaryCard}>
        <Text style={styles.summaryLabel}>상품명</Text>
        <Text style={styles.summaryValue}>{createdBankName || 'DONTTAZ 예금'}</Text>

        <View style={styles.divider} />

        <Text style={styles.summaryLabel}>신규 계좌번호</Text>
        <Text style={styles.summaryValue}>{createdAccountNumber || '-'}</Text>

        <View style={styles.divider} />

        <Text style={styles.summaryLabel}>계좌 별칭</Text>
        <Text style={styles.summaryValue}>{createdAccountAlias || '나의 통장'}</Text>
      </View>

      <View style={styles.buttonRow}>
        <View style={styles.halfButton}>
          <Button
            label="추가 연결"
            variant="outline"
            onPress={() => router.replace('/account-open-terms' as never)}
          />
        </View>
        <View style={styles.halfButton}>
          <Button label="확인" onPress={() => router.replace('/(tabs)/home' as never)} />
        </View>
      </View>
    </View>
  );
}
