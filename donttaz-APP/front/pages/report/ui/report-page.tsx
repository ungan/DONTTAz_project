import React from 'react';
import { SafeAreaView } from 'react-native';
import { PageHeader } from '@/shared/ui';
import { YearlyReport } from '@/widgets/yearly-report';
import { styles } from './report-page.styles';

export function ReportPage() {
  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="리포트" />
      <YearlyReport />
    </SafeAreaView>
  );
}
