import React, { useState } from 'react';
import { View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { PageHeader } from '@/shared/ui';
import { MonthlyReport } from '@/widgets/monthly-report';
import { ReportTab, ReportTabSelector } from '@/widgets/report-tab-selector';
import { YearlyReportPlaceholder } from '@/widgets/yearly-report';
import { styles } from './report-page.styles';

export function ReportPage() {
  const [activeTab, setActiveTab] = useState<ReportTab>('monthly');

  return (
    <SafeAreaView edges={['bottom']} style={styles.container}>
      <PageHeader title="리포트" />
      <View style={styles.tabSelector}>
        <ReportTabSelector activeTab={activeTab} onTabChange={setActiveTab} />
      </View>
      <View style={styles.content}>
        {activeTab === 'monthly' ? <MonthlyReport /> : <YearlyReportPlaceholder />}
      </View>
    </SafeAreaView>
  );
}
