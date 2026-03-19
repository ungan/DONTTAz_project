import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import Animated, { FadeInRight } from 'react-native-reanimated';
import AntDesign from '@expo/vector-icons/AntDesign';
import type { BurndownData, BurndownChartItem } from '@/entities/plan';
import { styles } from './burndown-daily-list.styles';

interface BurndownDailyListProps {
  data: BurndownData;
  onToggleView: () => void;
}

function formatWon(amount: number): string {
  return amount.toLocaleString('ko-KR') + '원';
}

function formatDate(dateStr: string): string {
  const [, month, day] = dateStr.split('-');
  return `${parseInt(month)}/${parseInt(day)}`;
}

function getDiff(item: BurndownChartItem): { text: string; color: string } | null {
  if (item.actualRemaining === null) return null;
  const diff = item.actualRemaining - item.idealRemaining;
  if (diff === 0) return { text: '±0', color: '#9CA3AF' };
  const sign = diff > 0 ? '+' : '';
  return { text: `${sign}${formatWon(diff)}`, color: diff > 0 ? '#22C55E' : '#EF4444' };
}

export function BurndownDailyList({ data, onToggleView }: BurndownDailyListProps) {
  return (
    <View style={styles.container}>
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.cardTitle}>플랜 달성 현황</Text>
          <TouchableOpacity style={styles.toggleButton} onPress={onToggleView}>
            <AntDesign name="line-chart" size={18} color="#6B7280" />
          </TouchableOpacity>
        </View>

        <View style={styles.tableHeader}>
          <Text style={[styles.tableHeaderText, { flex: 0.8, textAlign: 'left' }]}>날짜</Text>
          <Text style={[styles.tableHeaderText, { flex: 1.75, textAlign: 'center' }]}>실제잔액</Text>
          <Text style={[styles.tableHeaderText, { flex: 1.75, textAlign: 'right' }]}>이상 대비</Text>
        </View>

        {data.chartData.map((item, index) => {
          const diff = getDiff(item);
          return (
            <Animated.View
              key={item.date}
              entering={FadeInRight.duration(300).delay(index * 30)}
              style={styles.row}
            >
              <Text style={styles.cellDate}>{formatDate(item.date)}</Text>
              {item.actualRemaining !== null ? (
                <Text style={styles.cellActual}>{formatWon(item.actualRemaining)}</Text>
              ) : (
                <Text style={styles.cellActualEmpty}>-</Text>
              )}
              {diff ? (
                <Text style={[styles.cellDiff, { color: diff.color }]}>{diff.text}</Text>
              ) : (
                <Text style={[styles.cellDiff, { color: '#D1D5DB' }]}>-</Text>
              )}
            </Animated.View>
          );
        })}
      </View>
    </View>
  );
}
