import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { styles } from './report-tab-selector.styles';

export type ReportTab = 'yearly' | 'monthly';

interface ReportTabSelectorProps {
  activeTab: ReportTab;
  onTabChange: (tab: ReportTab) => void;
}

const TABS: { id: ReportTab; label: string }[] = [
  { id: 'yearly', label: '연간 리포트' },
  { id: 'monthly', label: '월간 리포트' },
];

export function ReportTabSelector({ activeTab, onTabChange }: ReportTabSelectorProps) {
  return (
    <View style={styles.container}>
      {TABS.map((tab) => {
        const isActive = activeTab === tab.id;
        return (
          <TouchableOpacity
            key={tab.id}
            style={[styles.tab, isActive && styles.tabActive]}
            onPress={() => onTabChange(tab.id)}
            activeOpacity={0.7}
          >
            <Text style={[styles.label, isActive && styles.labelActive]}>{tab.label}</Text>
          </TouchableOpacity>
        );
      })}
    </View>
  );
}
