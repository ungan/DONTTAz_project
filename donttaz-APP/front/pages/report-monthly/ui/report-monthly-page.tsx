import React from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { View } from 'react-native';
import { useLocalSearchParams } from 'expo-router';
import { PageHeader } from '@/shared/ui';
import { MonthlyReport } from '@/widgets/monthly-report';
import { styles } from './report-monthly-page.styles';

export function ReportMonthlyPage() {
  const { yearMonth } = useLocalSearchParams<{ yearMonth: string }>();

  return (
    <SafeAreaView edges={['bottom']} style={styles.container}>
      <PageHeader title="월별 리포트" />
      <View style={styles.content}>
        <MonthlyReport yearMonth={yearMonth} />
      </View>
    </SafeAreaView>
  );
}
