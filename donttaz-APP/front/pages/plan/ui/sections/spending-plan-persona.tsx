import React from 'react';
import { Text, View } from 'react-native';
import type { SpendingPlanData } from '@/entities/plan';
import { styles } from './spending-plan-persona.styles';

const PERSONA_EMOJI: Record<string, string> = {
  '식도락형': '🍜',
  '쇼핑족': '🛍️',
  '여가충전형': '🎨',
  '자기계발형': '📚',
  '재무관리형': '💰',
  '생활밀착형': '🏠',
  '균형추구형': '⚖️',
};

function riskLabelColor(label: string): { color: string } {
  if (label === '안정적') return { color: '#2B8A57' };
  if (label === '주의 필요') return { color: '#D97706' };
  return { color: '#E25B52' };
}

interface SpendingPlanPersonaProps {
  data: SpendingPlanData;
}

export function SpendingPlanPersona({ data }: SpendingPlanPersonaProps) {
  return (
    <View style={styles.card}>
      <Text style={styles.title}>이번 달 소비 페르소나</Text>
      <Text style={styles.persona}>
        {data.spendingPersona
          ? `${PERSONA_EMOJI[data.spendingPersona] ?? '✨'} ${data.spendingPersona}`
          : '분석 중'}
      </Text>
      <Text style={styles.helper}>AI가 분석한 나의 소비 유형이에요.</Text>

      <View style={styles.riskRow}>
        <Text style={styles.riskValue}>위험도 </Text>
        <Text style={styles.riskValue}>
          {data.riskPercent === null ? '-' : `${data.riskPercent}%`}
        </Text>
        <Text style={styles.riskDot}> · </Text>
        <Text style={[styles.riskLabel, riskLabelColor(data.riskLevelLabel)]}>
          {data.riskLevelLabel}
        </Text>
      </View>
    </View>
  );
}
