import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import type { AccountOpenTermItem } from '../types/account-open';
import { styles } from './account-open-terms-section.styles';

interface AccountOpenTermsSectionProps {
  title: string;
  subtitle?: string;
  allAgreed: boolean;
  items: AccountOpenTermItem[];
  onToggleAll: () => void;
  onToggleItem: (id: string) => void;
}

function CheckIcon({ active }: { active: boolean }) {
  return (
    <View style={[styles.checkCircle, active && styles.checkCircleActive]}>
      <Ionicons name="checkmark" size={14} color={active ? '#FFFFFF' : '#D1D5DB'} />
    </View>
  );
}

export function AccountOpenTermsSection({
  title,
  subtitle,
  allAgreed,
  items,
  onToggleAll,
  onToggleItem,
}: AccountOpenTermsSectionProps) {
  return (
    <View style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>{title}</Text>
        {subtitle ? <Text style={styles.sectionSubtitle}>{subtitle}</Text> : null}
      </View>

      <View style={styles.card}>
        {/* 전체 동의 행 */}
        <TouchableOpacity style={styles.allRow} activeOpacity={0.7} onPress={onToggleAll}>
          <CheckIcon active={allAgreed} />
          <Text style={styles.allLabel}>{title} 전체 동의</Text>
        </TouchableOpacity>

        <View style={styles.divider} />

        {/* 개별 항목 */}
        {items.map((item, index) => (
          <View key={item.id}>
            <TouchableOpacity
              style={styles.itemRow}
              activeOpacity={0.7}
              onPress={() => onToggleItem(item.id)}
            >
              <CheckIcon active={item.isAgreed} />
              <Text style={styles.itemLabel} numberOfLines={2}>
                {item.title}
              </Text>
              <Ionicons name="chevron-forward" size={16} color="#C4C9D4" />
            </TouchableOpacity>
            {index < items.length - 1 && <View style={styles.innerDivider} />}
          </View>
        ))}
      </View>
    </View>
  );
}
