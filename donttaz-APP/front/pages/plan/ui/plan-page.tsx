import { useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, ActivityIndicator, Text } from 'react-native';
import { PageHeader } from '@/shared/ui';
import { BurndownChart } from '@/widgets/burndown-chart';
import { BurndownDailyList } from '@/widgets/burndown-daily-list';
import { useBurndown, useBurndownStats } from '@/entities/plan';
import { styles } from './plan-page.styles';

function getCurrentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

export function PlanPage() {
  const [yearMonth] = useState(getCurrentYearMonth());
  const [isListView, setIsListView] = useState(false);
  const { data, isPending, error } = useBurndown(yearMonth);
  const { vsIdealPercent, daysUntilBankrupt } = useBurndownStats(data);

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="소비 플랜" />
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {isPending && <ActivityIndicator color="#5899FD" style={styles.loader} />}
        {!!error && <Text style={styles.errorText}>{error}</Text>}
        {data && !isListView && (
          <BurndownChart
            data={data}
            yearMonth={yearMonth}
            vsIdealPercent={vsIdealPercent}
            daysUntilBankrupt={daysUntilBankrupt}
            onToggleView={() => setIsListView(true)}
          />
        )}
        {data && isListView && (
          <BurndownDailyList
            data={data}
            onToggleView={() => setIsListView(false)}
          />
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
