import React from 'react';
import { Text, View } from 'react-native';
import type { SpendingPlanData } from '@/entities/plan';
import { styles } from './spending-plan-summary.styles';

function formatWon(amount: number): string {
  return `${amount.toLocaleString('ko-KR')}원`;
}

function formatMonth(yearMonth: string): string {
  const [, month] = yearMonth.split('-');
  return `${parseInt(month, 10)}월`;
}

interface SpendingPlanSummaryProps {
  data: SpendingPlanData;
}

export function SpendingPlanSummary({ data }: SpendingPlanSummaryProps) {
  return (
    <View style={styles.card}>
      <View style={styles.badge}>
        <Text style={styles.badgeText}>{data.budgetStrategyLabel}</Text>
      </View>
      <Text style={styles.title}>{formatMonth(data.targetYearMonth)} AI 소비 플랜</Text>
      <Text style={styles.subtitle}>
        최근 소비 패턴을 바탕으로 이번 달 예산안을 만들었어요.
      </Text>

      <Text style={styles.totalLabel}>권장 소비 예산</Text>
      <Text style={styles.totalAmount}>{formatWon(data.totalBudget)}</Text>

      <View style={styles.statRow}>
        <View style={styles.statCard}>
          <Text style={styles.statLabel}>금고 격리 금액</Text>
          <Text style={styles.statValue}>{formatWon(data.sealedAmount)}</Text>
        </View>
        <View style={styles.statSpacer} />
        <View style={styles.statCard}>
          <Text style={styles.statLabel}>월급일</Text>
          <Text style={styles.statValue}>
            {data.paydayDay ? `${data.paydayDay}일 기준` : '정보 없음'}
          </Text>
        </View>
      </View>

      {data.surplusAmount > 0 && (
        <Text style={styles.footnote}>
          이번 달에는 잉여 자금으로 {formatWon(data.surplusAmount)} 정도를 더 남길 수 있어요.
        </Text>
      )}
    </View>
  );
}
