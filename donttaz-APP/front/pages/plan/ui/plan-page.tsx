import { useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, ActivityIndicator, Text, View } from 'react-native';
import { Button, PageHeader } from '@/shared/ui';
import burndownPreview from '@/shared/mock/data/burndown-surviving.json';
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
  const { data, isPending, error, noPlan, hasFetched, reload } = useBurndown(yearMonth, { source: 'plan-page' });
  const { vsIdealPercent, daysUntilBankrupt } = useBurndownStats(data);
  const previewData = burndownPreview.data;
  const previewStats = useBurndownStats(previewData);
  const hasChartData = !!data && data.chartData.length > 0;
  const showLoading = isPending && !data;
  const showError = !!error && !data && !noPlan;
  const showEmpty = hasFetched && noPlan;

  return (
    <SafeAreaView style={styles.container}>
      <PageHeader title="소비 플랜" />
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {showLoading && <ActivityIndicator color="#5899FD" style={styles.loader} />}

        {showError && (
          <View style={styles.stateCard}>
            <Text style={styles.stateTitle}>번다운 데이터를 불러오지 못했어요.</Text>
            <Text style={styles.stateDescription}>{error}</Text>
            <View style={styles.stateButtonWrap}>
              <Button label="다시 시도" variant="outline" onPress={reload} />
            </View>
          </View>
        )}

        {showEmpty && (
          <View style={styles.previewSection}>
            <View pointerEvents="none" style={styles.previewChartWrap}>
              <BurndownChart
                data={previewData}
                yearMonth={previewData.targetYearMonth}
                vsIdealPercent={previewStats.vsIdealPercent}
                daysUntilBankrupt={previewStats.daysUntilBankrupt}
                onToggleView={() => {}}
              />
            </View>
            <View style={styles.previewOverlay}>
              <View style={styles.previewMessageCard}>
                <Text style={styles.stateTitle}>소비 계획을 먼저 만들어주세요.</Text>
                <Text style={styles.stateDescription}>
                  계획이 만들어지면 번다운 차트로 이번 달 소비 흐름을 볼 수 있어요.
                </Text>
                <View style={styles.stateButtonWrap}>
                  <Button label="새로고침" variant="outline" onPress={reload} />
                </View>
              </View>
            </View>
          </View>
        )}

        {hasChartData && !isListView && data && (
          <BurndownChart
            data={data}
            yearMonth={yearMonth}
            vsIdealPercent={vsIdealPercent}
            daysUntilBankrupt={daysUntilBankrupt}
            onToggleView={() => setIsListView(true)}
          />
        )}

        {hasChartData && isListView && data && (
          <BurndownDailyList data={data} onToggleView={() => setIsListView(false)} />
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
