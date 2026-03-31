import React, { useEffect, useState } from 'react';
import { View, Text, ScrollView, TouchableOpacity, ActivityIndicator } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useMonthlyReport, SpendingReportFeedbackItem } from '@/entities/report';
import { MonthlyReportDonutChart } from './monthly-report-donut-chart';
import { MonthlyReportLegend } from './monthly-report-legend';
import { MonthlyReportCategoryList } from './monthly-report-category-list';
import { styles } from './monthly-report.styles';

function getDefaultReportYearMonth(): string {
  const baseDate = new Date();
  baseDate.setMonth(baseDate.getMonth() - 1);

  const year = baseDate.getFullYear();
  const month = String(baseDate.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

function shiftYearMonth(yearMonth: string, diff: number): string {
  const [yearText, monthText] = yearMonth.split('-');
  const year = Number(yearText);
  const month = Number(monthText);

  if (!Number.isFinite(year) || !Number.isFinite(month)) {
    return getDefaultReportYearMonth();
  }

  const nextDate = new Date(year, month - 1, 1);
  nextDate.setMonth(nextDate.getMonth() + diff);
  return `${nextDate.getFullYear()}-${String(nextDate.getMonth() + 1).padStart(2, '0')}`;
}

function formatYearMonthLabel(yearMonth: string): string {
  const [year, month] = yearMonth.split('-');

  if (!year || !month) {
    return yearMonth;
  }

  return `${year}년 ${month}월`;
}

function getFeedbackTypeLabel(type: SpendingReportFeedbackItem['type']) {
  switch (type) {
    case 'persona':
      return '소비 성향';
    case 'warning':
      return '주의';
    case 'pattern':
      return '패턴';
    case 'positive':
      return '강점';
    default:
      return '피드백';
  }
}

function getFeedbackTypeStyle(type: SpendingReportFeedbackItem['type']) {
  switch (type) {
    case 'persona':
      return {
        badge: styles.feedbackBadgePersona,
        text: styles.feedbackBadgeTextPersona,
      };
    case 'warning':
      return {
        badge: styles.feedbackBadgeWarning,
        text: styles.feedbackBadgeTextWarning,
      };
    case 'pattern':
      return {
        badge: styles.feedbackBadgePattern,
        text: styles.feedbackBadgeTextPattern,
      };
    case 'positive':
      return {
        badge: styles.feedbackBadgePositive,
        text: styles.feedbackBadgeTextPositive,
      };
    default:
      return {
        badge: styles.feedbackBadgePattern,
        text: styles.feedbackBadgeTextPattern,
      };
  }
}

function formatWon(amount: number) {
  return `${Math.abs(amount).toLocaleString()}원`;
}

interface MonthlyReportProps {
  yearMonth?: string;
}

export function MonthlyReport({ yearMonth: yearMonthProp }: MonthlyReportProps) {
  const resolvedInitialYearMonth = yearMonthProp ?? getDefaultReportYearMonth();
  const [selectedYearMonth, setSelectedYearMonth] = useState(resolvedInitialYearMonth);

  useEffect(() => {
    setSelectedYearMonth(resolvedInitialYearMonth);
  }, [resolvedInitialYearMonth]);

  const latestYearMonth = getDefaultReportYearMonth();
  const oldestYearMonth = shiftYearMonth(latestYearMonth, -11);
  const isLatest = selectedYearMonth >= latestYearMonth;
  const isOldest = selectedYearMonth <= oldestYearMonth;

  const { summary, categories, topCategory, budgetGap, viewState, statusMessage, isPending, error } =
    useMonthlyReport(selectedYearMonth);

  const budgetGapLabel = budgetGap >= 0 ? '저축 성공액' : '예산 초과액';
  const budgetGapAmount = formatWon(budgetGap);

  if (isPending) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color="#5899FD" />
      </View>
    );
  }

  if (viewState === 'no-plan') {
    return (
      <View style={styles.centerCardWrap}>
        <View style={styles.stateCard}>
          <Text style={styles.stateTitle}>먼저 이 달의 소비 플랜이 필요해요.</Text>
          <Text style={styles.stateDescription}>
            {statusMessage ||
              '현재 선택한 월에 맞춤 소비 플랜이 없어 리포트를 만들 수 없습니다.'}
          </Text>
        </View>
      </View>
    );
  }

  if (viewState === 'error') {
    return (
      <View style={styles.centerCardWrap}>
        <View style={styles.stateCard}>
          <Text style={styles.stateTitle}>리포트를 불러오지 못했어요.</Text>
          <Text style={styles.stateDescription}>
            {error || statusMessage || '잠시 후 다시 시도해주세요.'}
          </Text>
        </View>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.contentContainer}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.monthNav}>
        <TouchableOpacity
          onPress={() => setSelectedYearMonth(shiftYearMonth(selectedYearMonth, -1))}
          disabled={isOldest}
          hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
        >
          <Ionicons name="chevron-back" size={20} color={isOldest ? '#D1D5DB' : '#374151'} />
        </TouchableOpacity>
        <Text style={styles.monthNavText}>{formatYearMonthLabel(selectedYearMonth)}</Text>
        <TouchableOpacity
          onPress={() => setSelectedYearMonth(shiftYearMonth(selectedYearMonth, 1))}
          disabled={isLatest}
          hitSlop={{ top: 12, bottom: 12, left: 12, right: 12 }}
        >
          <Ionicons name="chevron-forward" size={20} color={isLatest ? '#D1D5DB' : '#374151'} />
        </TouchableOpacity>
      </View>

      <View style={styles.header}>
        <View>
          <Text style={styles.totalLabel}>총 지출</Text>
          <Text style={styles.totalAmount}>{`${summary.totalSpent.toLocaleString()}원`}</Text>
        </View>
      </View>

      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>총 예산</Text>
          <Text style={styles.summaryCardValue}>{`${summary.totalBudget.toLocaleString()}원`}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>달성률</Text>
          <Text style={styles.summaryCardValue}>{summary.achievementRate.toFixed(1)}%</Text>
        </View>
      </View>

      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>소비 페르소나</Text>
          <Text style={styles.summaryCardValue}>
            {summary.spendingPersona ?? '분석 중'}
          </Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>최다 지출</Text>
          <Text style={styles.summaryCardValue}>
            {topCategory ? topCategory.category : '데이터 없음'}
          </Text>
        </View>
      </View>

      <View style={styles.summaryRow}>
        <View style={[styles.summaryCard, styles.summaryCardFull]}>
          <Text style={styles.summaryCardLabel}>{budgetGapLabel}</Text>
          <Text style={styles.summaryCardValue}>{budgetGapAmount}</Text>
        </View>
      </View>

      <View style={styles.feedbackCard}>
        <View style={styles.feedbackTitleRow}>
          <Ionicons name="bulb-outline" size={16} color="#335C9E" />
          <Text style={styles.feedbackTitle}>AI 종합 피드백</Text>
        </View>
        <Text style={styles.feedbackBody}>
          {summary.aiOverallFeedback ||
            '이번 달 소비 패턴을 분석 중입니다.'}
        </Text>
      </View>

      {viewState === 'empty' ? (
        <View style={styles.emptyCard}>
          <Text style={styles.emptyTitle}>표시할 소비 데이터가 없습니다.</Text>
          <Text style={styles.emptyDescription}>
            이 달에는 아직 지출 내역이 없어 예산 상태만 표시해요.
          </Text>
        </View>
      ) : (
        <>
          <View style={styles.divider} />
          <Text style={styles.sectionTitle}>내 소비 한 눈에 보기</Text>
          <MonthlyReportDonutChart categories={categories} />
          <MonthlyReportLegend categories={categories} />

          <Text style={styles.sectionTitle}>카테고리별 소비</Text>
          <MonthlyReportCategoryList categories={categories} />
        </>
      )}

      {summary.personalizedFeedback.length > 0 && (() => {
        const typeOrder: Record<string, number> = { persona: 0, pattern: 1, positive: 2, warning: 3 };
        const grouped = [...summary.personalizedFeedback]
          .sort((a, b) => (typeOrder[a.type] ?? 4) - (typeOrder[b.type] ?? 4))
          .reduce<{ type: SpendingReportFeedbackItem['type']; items: SpendingReportFeedbackItem[] }[]>((acc, item) => {
            const last = acc[acc.length - 1];
            if (last && last.type === item.type) {
              last.items.push(item);
            } else {
              acc.push({ type: item.type, items: [item] });
            }
            return acc;
          }, []);

        return (
          <>
            <Text style={styles.sectionTitle}>맞춤 피드백</Text>
            <View style={styles.personalizedFeedbackCard}>
              {grouped.map((group, gIndex) => (
                <View
                  key={group.type}
                  style={[
                    styles.personalizedFeedbackItem,
                    gIndex === grouped.length - 1 && styles.personalizedFeedbackItemLast,
                  ]}
                >
                  {group.items.map((item, iIndex) => (
                    <View key={iIndex} style={iIndex > 0 ? styles.feedbackMessageGap : undefined}>
                      <View style={styles.personalizedFeedbackHeader}>
                        {iIndex === 0 && (
                          <View style={[styles.feedbackBadge, getFeedbackTypeStyle(group.type).badge]}>
                            <Text style={[styles.personalizedFeedbackType, getFeedbackTypeStyle(group.type).text]}>
                              {getFeedbackTypeLabel(group.type)}
                            </Text>
                          </View>
                        )}
                        {item.categoryName ? (
                          <Text style={styles.personalizedFeedbackCategory}>{item.categoryName}</Text>
                        ) : null}
                      </View>
                      <Text style={styles.personalizedFeedbackMessage}>{item.message}</Text>
                    </View>
                  ))}
                </View>
              ))}
            </View>
          </>
        );
      })()}
    </ScrollView>
  );
}
