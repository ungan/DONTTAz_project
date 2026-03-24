import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { styles } from './vault-tab-bar.styles';

export type VaultTab = 'default' | 'wishlist';

interface VaultTabBarProps {
  activeTab: VaultTab;
  onTabChange: (tab: VaultTab) => void;
}

const TABS: { key: VaultTab; label: string }[] = [
  { key: 'default', label: '금고' },
  { key: 'wishlist', label: '위시리스트' },
];

export function VaultTabBar({ activeTab, onTabChange }: VaultTabBarProps) {
  return (
    <View style={styles.container}>
      {TABS.map((tab) => (
        <TouchableOpacity
          key={tab.key}
          style={[styles.tab, activeTab === tab.key && styles.tabActive]}
          activeOpacity={0.8}
          onPress={() => onTabChange(tab.key)}
        >
          <Text style={[styles.label, activeTab === tab.key && styles.labelActive]}>
            {tab.label}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}
