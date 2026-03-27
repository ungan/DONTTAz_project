import React, { useEffect, useState } from 'react';
import { View, Text, ScrollView, ActivityIndicator } from 'react-native';
import { SelectInput } from '@/shared/ui';
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

function buildMonthOptions(selectedYearMonth: string): string[] {
  const options = new Set<string>();

  for (let i = 0; i < 12; i += 1) {
    options.add(shiftYearMonth(getDefaultReportYearMonth(), -i));
  }

  options.add(selectedYearMonth);

  return Array.from(options).sort((a, b) => (a < b ? 1 : -1));
}

function formatYearMonthLabel(yearMonth: string): string {
  const [year, month] = yearMonth.split('-');

  if (!year || !month) {
    return yearMonth;
  }

  return `${year}\uB144 ${month}\uC6D4`;
}

function getFeedbackTypeLabel(type: SpendingReportFeedbackItem['type']) {
  switch (type) {
    case 'persona':
      return '\uC18C\uBE44 \uC131\uD5A5';
    case 'warning':
      return '\uC8FC\uC758';
    case 'pattern':
      return '\uD328\uD134';
    case 'positive':
      return '\uAC15\uC810';
    default:
      return '\uD53C\uB4DC\uBC31';
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
  return `${Math.abs(amount).toLocaleString()}\uC6D0`;
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

  const { summary, categories, topCategory, budgetGap, viewState, statusMessage, isPending, error } =
    useMonthlyReport(selectedYearMonth);
  const monthOptions = buildMonthOptions(selectedYearMonth);

  const month = summary.yearMonth.split('-')[1];
  const achievementText =
    summary.achievementRate > 100
      ? '\uC608\uC0B0 \uCD08\uACFC'
      : '\uC608\uC0B0 \uBC94\uC704';
  const budgetGapLabel =
    budgetGap >= 0
      ? `\uC608\uC0B0 \uC5EC\uC720 ${formatWon(budgetGap)}`
      : `\uCD08\uACFC \uC9C0\uCD9C ${formatWon(budgetGap)}`;

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
          <Text style={styles.stateTitle}>
            {'\uBA3C\uC800 \uC774 \uB2EC\uC758 \uC18C\uBE44 \uD50C\uB79C\uC774 \uD544\uC694\uD574\uC694.'}
          </Text>
          <Text style={styles.stateDescription}>
            {statusMessage ||
              '\uD604\uC7AC \uC120\uD0DD\uD55C \uC6D4\uC5D0 \uB9DE\uCDA4 \uC18C\uBE44 \uD50C\uB79C\uC774 \uC5C6\uC5B4 \uB9AC\uD3EC\uD2B8\uB97C \uB9CC\uB4E4 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.'}
          </Text>
        </View>
      </View>
    );
  }

  if (viewState === 'error') {
    return (
      <View style={styles.centerCardWrap}>
        <View style={styles.stateCard}>
          <Text style={styles.stateTitle}>
            {'\uB9AC\uD3EC\uD2B8\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC5B4\uC694.'}
          </Text>
          <Text style={styles.stateDescription}>
            {error || statusMessage || '\uC7A0\uC2DC \uD6C4 \uB2E4\uC2DC \uC2DC\uB3C4\uD574\uC8FC\uC138\uC694.'}
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
      <View style={styles.header}>
        <View>
          <Text style={styles.totalLabel}>{`${month}\uC6D4 \uCD1D \uC9C0\uCD9C`}</Text>
          <Text style={styles.totalAmount}>{`${summary.totalSpent.toLocaleString()}\uC6D0`}</Text>
        </View>
        <View style={styles.headerRight}>
          <View style={styles.monthBadge}>
            <Text style={styles.monthText}>{`${month}\uC6D4`}</Text>
          </View>
          <View
            style={[
              styles.statusBadge,
              summary.achievementRate > 100 ? styles.statusBadgeDanger : styles.statusBadgeSafe,
            ]}
          >
            <Text
              style={[
                styles.statusText,
                summary.achievementRate > 100 ? styles.statusTextDanger : styles.statusTextSafe,
              ]}
            >
              {achievementText}
            </Text>
          </View>
        </View>
      </View>

      <View style={styles.selectorWrap}>
        <SelectInput
          label={'\uC870\uD68C \uC6D4'}
          options={monthOptions}
          value={selectedYearMonth}
          onChange={setSelectedYearMonth}
          getOptionLabel={formatYearMonthLabel}
        />
      </View>

      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>{'\uCD1D \uC608\uC0B0'}</Text>
          <Text style={styles.summaryCardValue}>{`${summary.totalBudget.toLocaleString()}\uC6D0`}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>{'\uB2EC\uC131\uB960'}</Text>
          <Text style={styles.summaryCardValue}>{summary.achievementRate.toFixed(1)}%</Text>
        </View>
      </View>

      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>{'\uBD09\uC778 \uAE08\uC561'}</Text>
          <Text style={styles.summaryCardValue}>{`${summary.sealedAmount.toLocaleString()}\uC6D0`}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>{'\uC18C\uBE44 \uD398\uB974\uC18C\uB098'}</Text>
          <Text style={styles.summaryCardValue}>
            {summary.spendingPersona ?? '\uBD84\uC11D \uC911'}
          </Text>
        </View>
      </View>

      <View style={styles.summaryRow}>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>{'\uC608\uC0B0 \uCC28\uC774'}</Text>
          <Text style={styles.summaryCardValue}>{budgetGapLabel}</Text>
        </View>
        <View style={styles.summaryCard}>
          <Text style={styles.summaryCardLabel}>{'\uCD5C\uB300 \uC9C0\uCD9C \uCE74\uD14C\uACE0\uB9AC'}</Text>
          <Text style={styles.summaryCardValue}>
            {topCategory ? topCategory.category : '\uB370\uC774\uD130 \uC5C6\uC74C'}
          </Text>
        </View>
      </View>

      <View style={styles.feedbackCard}>
        <Text style={styles.feedbackTitle}>{'AI \uC885\uD569 \uD53C\uB4DC\uBC31'}</Text>
        <Text style={styles.feedbackBody}>
          {summary.aiOverallFeedback ||
            '\uC774\uBC88 \uB2EC \uC18C\uBE44 \uD328\uD134\uC744 \uBD84\uC11D \uC911\uC785\uB2C8\uB2E4.'}
        </Text>
      </View>

      {viewState === 'empty' ? (
        <View style={styles.emptyCard}>
          <Text style={styles.emptyTitle}>
            {'\uD45C\uC2DC\uD560 \uC18C\uBE44 \uB370\uC774\uD130\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4.'}
          </Text>
          <Text style={styles.emptyDescription}>
            {
              '\uC774 \uB2EC\uC5D0\uB294 \uC544\uC9C1 \uC9C0\uCD9C \uB0B4\uC5ED\uC774 \uC5C6\uC5B4 \uC608\uC0B0 \uC0C1\uD0DC\uB97C \uB2E8\uC21C \uC694\uC57D\uB9CC \uD45C\uC2DC\uD574\uC694.'
            }
          </Text>
        </View>
      ) : (
        <>
          <MonthlyReportDonutChart categories={categories} />
          <MonthlyReportLegend categories={categories} />

          <Text style={styles.sectionTitle}>{'\uCE74\uD14C\uACE0\uB9AC\uBCC4 \uC18C\uBE44'}</Text>
          <MonthlyReportCategoryList categories={categories} />
        </>
      )}

      {summary.personalizedFeedback.length > 0 && (
        <>
          <Text style={styles.sectionTitle}>{'\uB9DE\uCDA4 \uD53C\uB4DC\uBC31'}</Text>
          <View style={styles.personalizedFeedbackCard}>
            {summary.personalizedFeedback.map((item, index) => (
              <View
                key={`${item.type}-${item.categoryName ?? 'overall'}-${index}`}
                style={[
                  styles.personalizedFeedbackItem,
                  index === summary.personalizedFeedback.length - 1 && styles.personalizedFeedbackItemLast,
                ]}
              >
                <View style={styles.personalizedFeedbackHeader}>
                  <View style={[styles.feedbackBadge, getFeedbackTypeStyle(item.type).badge]}>
                    <Text
                      style={[
                        styles.personalizedFeedbackType,
                        getFeedbackTypeStyle(item.type).text,
                      ]}
                    >
                      {getFeedbackTypeLabel(item.type)}
                    </Text>
                  </View>
                  {item.categoryName ? (
                    <Text style={styles.personalizedFeedbackCategory}>{item.categoryName}</Text>
                  ) : null}
                </View>
                <Text style={styles.personalizedFeedbackMessage}>{item.message}</Text>
              </View>
            ))}
          </View>
        </>
      )}
    </ScrollView>
  );
}
