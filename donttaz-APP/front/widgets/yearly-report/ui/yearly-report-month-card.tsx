import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { FontAwesome5, Ionicons } from '@expo/vector-icons';
import { styles } from './yearly-report-month-card.styles';

interface Props {
  year: string;
  month: string;
  onPress: () => void;
}

export function YearlyReportMonthCard({ year, month, onPress }: Props) {
  const monthInt = parseInt(month, 10);

  return (
    <TouchableOpacity style={styles.container} onPress={onPress} activeOpacity={0.8}>
      <View style={styles.iconWrapper}>
        <FontAwesome5 name="calendar-alt" size={26} color="#FFFFFF" />
      </View>
      <View style={styles.textWrapper}>
        <Text style={styles.monthText}>{year}년 {monthInt}월 소비 리포트</Text>
        <View style={styles.detailRow}>
          <Text style={styles.detailText}>자세히 보기</Text>
          <Ionicons name="chevron-forward" size={14} color="#6B7280" />
        </View>
      </View>
    </TouchableOpacity>
  );
}
