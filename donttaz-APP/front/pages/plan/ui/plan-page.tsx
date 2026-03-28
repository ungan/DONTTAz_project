import { useEffect, useState } from 'react';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ScrollView, ActivityIndicator, Text, TouchableOpacity, View } from 'react-native';
import { Button, PageHeader } from '@/shared/ui';
import burndownPreview from '@/shared/mock/data/burndown-surviving.json';
import { useBurndown, useBurndownStats, useSpendingPlan } from '@/entities/plan';
import { BurndownChart } from '@/widgets/burndown-chart';
import { BurndownDailyList } from '@/widgets/burndown-daily-list';
import { styles } from './plan-page.styles';
import {
  SpendingPlanCategoryList,
  SpendingPlanPersona,
  SpendingPlanPieChart,
  SpendingPlanRecommendations,
  SpendingPlanSummary,
} from './sections';

function getCurrentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

type PlanTab = 'plan' | 'dashboard';

export function PlanPage() {
  const [yearMonth] = useState(getCurrentYearMonth());
  const [activeTab, setActiveTab] = useState<PlanTab>('plan');
  const [isListView, setIsListView] = useState(false);

  const {
    data: planData,
    isPending: isPlanPending,
    error: planError,
    hasFetched: hasPlanFetched,
    reload: reloadPlan,
  } = useSpendingPlan(yearMonth, { source: 'plan-page' });

  const {
    data,
    isPending,
    error,
    noPlan,
    hasFetched,
    reload,
  } = useBurndown(yearMonth, { source: 'plan-page', enabled: activeTab === 'dashboard' });

  const { vsIdealPercent, daysUntilBankrupt } = useBurndownStats(data);
  const previewData = burndownPreview.data;
  const previewStats = useBurndownStats(previewData);
  const hasChartData = !!data && data.chartData.length > 0;
  const showLoading = isPending && !data;
  const showError = !!error && !data && !noPlan;
  const showEmpty = hasFetched && noPlan;
  const showPlanLoading = isPlanPending && !planData;
  const showPlanError = !!planError && !planData;
  const showPlanEmpty = hasPlanFetched && !isPlanPending && !planError && !planData;

  useEffect(() => {
    if (planData && noPlan) {
      reload();
    }
  }, [planData, noPlan, reload]);

  return (
    <SafeAreaView edges={['bottom']} style={styles.container}>
      <PageHeader title="플랜" />
      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        <View style={styles.tabRow}>
          <TouchableOpacity
            style={[styles.tabButton, activeTab === 'plan' && styles.tabButtonActive]}
            onPress={() => setActiveTab('plan')}
            activeOpacity={0.8}
          >
            <Text style={[styles.tabLabel, activeTab === 'plan' && styles.tabLabelActive]}>
              소비 플랜
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tabButton, activeTab === 'dashboard' && styles.tabButtonActive]}
            onPress={() => setActiveTab('dashboard')}
            activeOpacity={0.8}
          >
            <Text style={[styles.tabLabel, activeTab === 'dashboard' && styles.tabLabelActive]}>
              대시보드
            </Text>
          </TouchableOpacity>
        </View>

        {activeTab === 'plan' && (
          <>
            {showPlanLoading && (
              <View style={styles.loaderWrap}>
                <ActivityIndicator color="#5899FD" style={styles.loader} />
                <Text style={styles.helperText}>이번 달 소비 플랜을 만들고 있어요.</Text>
              </View>
            )}

            {showPlanError && (
              <View style={styles.stateCard}>
                <Text style={styles.stateTitle}>소비 플랜을 불러오지 못했어요.</Text>
                <Text style={styles.stateDescription}>{planError}</Text>
                <View style={styles.stateButtonWrap}>
                  <Button label="다시 시도" variant="outline" onPress={reloadPlan} />
                </View>
              </View>
            )}

            {showPlanEmpty && (
              <View style={styles.stateCard}>
                <Text style={styles.stateTitle}>아직 보여드릴 소비 플랜이 없어요.</Text>
                <Text style={styles.stateDescription}>
                  이번 달 예산안을 다시 만들어서 불러와볼게요.
                </Text>
                <View style={styles.stateButtonWrap}>
                  <Button label="플랜 다시 만들기" variant="outline" onPress={reloadPlan} />
                </View>
              </View>
            )}

            {planData && (
              <>
                <View style={styles.firstSection}>
                  <SpendingPlanSummary data={planData} />
                </View>

                <View style={styles.section}>
                  <SpendingPlanPieChart
                    categoryPlans={planData.categoryPlans}
                  />
                </View>

                <View style={styles.section}>
                  <SpendingPlanPersona data={planData} />
                </View>

                <View style={styles.section}>
                  <SpendingPlanRecommendations
                    recommendations={planData.personalizedRecommendations}
                  />
                </View>


              </>
            )}
          </>
        )}

        {activeTab === 'dashboard' && (
          <>

            {showLoading && (
              <View style={styles.loaderWrap}>
                <ActivityIndicator color="#5899FD" style={styles.loader} />
                <Text style={styles.helperText}>실시간 소비 현황을 불러오고 있어요.</Text>
              </View>
            )}

            {showError && (
              <View style={[styles.stateCard, styles.section]}>
                <Text style={styles.stateTitle}>대시보드를 불러오지 못했어요.</Text>
                <Text style={styles.stateDescription}>{error}</Text>
                <View style={styles.stateButtonWrap}>
                  <Button label="다시 시도" variant="outline" onPress={reload} />
                </View>
              </View>
            )}

            {showEmpty && (
              <View style={[styles.previewSection, styles.section]}>
                <View pointerEvents="none" style={styles.previewChartWrap}>
                  <BurndownChart
                    data={previewData}
                    vsIdealPercent={previewStats.vsIdealPercent}
                    daysUntilBankrupt={previewStats.daysUntilBankrupt}
                    onToggleView={() => {}}
                  />
                </View>
                <View style={styles.previewOverlay}>
                  <View style={styles.previewMessageCard}>
                    <Text style={styles.stateTitle}>먼저 소비 플랜을 확인해보세요.</Text>
                    <Text style={styles.stateDescription}>
                      소비 플랜이 준비되면 대시보드에서 예산 추이를 함께 볼 수 있어요.
                    </Text>
                    <View style={styles.stateButtonWrap}>
                      <Button
                        label="소비 플랜으로 돌아가기"
                        variant="outline"
                        onPress={() => setActiveTab('plan')}
                      />
                    </View>
                  </View>
                </View>
              </View>
            )}

            {hasChartData && !isListView && data && (
              <View style={styles.section}>
                <BurndownChart
                  data={data}
                  vsIdealPercent={vsIdealPercent}
                  daysUntilBankrupt={daysUntilBankrupt}
                  onToggleView={() => setIsListView(true)}
                />
              </View>
            )}

            {hasChartData && isListView && data && (
              <View style={styles.section}>
                <BurndownDailyList data={data} onToggleView={() => setIsListView(false)} />
              </View>
            )}

          </>
        )}
      </ScrollView>
    </SafeAreaView>
  );
}
