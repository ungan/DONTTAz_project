import React from 'react';
import { Text, View } from 'react-native';
import type { SpendingPlanData } from '@/entities/plan';
import { styles } from './spending-plan-persona.styles';

interface SpendingPlanPersonaProps {
  data: SpendingPlanData;
}

export function SpendingPlanPersona({ data }: SpendingPlanPersonaProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>이번 달 소비 성향</Text>
      <View style={styles.row}>
        <View style={styles.left}>
          <Text style={styles.label}>소비 페르소나</Text>
          <Text style={styles.persona}>{data.spendingPersona ?? '분석 중'}</Text>
          <Text style={styles.helper}>
            이번 달 소비 흐름을 가장 잘 설명하는 패턴을 보여드려요.
          </Text>
        </View>
        <View style={styles.right}>
          <View style={styles.riskBadge}>
            <Text style={styles.riskBadgeText}>{data.riskLevelLabel}</Text>
          </View>
          <Text style={styles.riskValue}>
            {data.riskPercent === null ? '-' : `${data.riskPercent}%`}
          </Text>
          <Text style={styles.riskCaption}>위험도</Text>
        </View>
      </View>
    </View>
  );
}
