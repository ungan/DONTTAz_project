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

function AgreementRow({
  label,
  isAgreed,
  onPress,
  isSummary = false,
}: {
  label: string;
  isAgreed: boolean;
  onPress: () => void;
  isSummary?: boolean;
}) {
  return (
    <TouchableOpacity style={[styles.row, isSummary && styles.summaryRow]} activeOpacity={0.8} onPress={onPress}>
      <View style={styles.rowLeft}>
        <View style={[styles.iconCircle, isAgreed && styles.iconCircleActive]}>
          <Ionicons
            name={isAgreed ? 'checkmark' : 'ellipse-outline'}
            size={16}
            color={isAgreed ? '#FFFFFF' : '#9CA3AF'}
          />
        </View>
        <Text style={[styles.rowLabel, isSummary && styles.summaryLabel]}>{label}</Text>
      </View>
      <Ionicons name="chevron-down" size={18} color="#9CA3AF" />
    </TouchableOpacity>
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
      <Text style={styles.title}>{title}</Text>
      {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}

      <AgreementRow
        label={`${title} 모두 동의`}
        isAgreed={allAgreed}
        onPress={onToggleAll}
        isSummary
      />

      <View style={styles.list}>
        {items.map((item) => (
          <AgreementRow
            key={item.id}
            label={item.title}
            isAgreed={item.isAgreed}
            onPress={() => onToggleItem(item.id)}
          />
        ))}
      </View>
    </View>
  );
}
