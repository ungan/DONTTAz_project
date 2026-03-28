import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Image } from 'expo-image';
import { styles } from './favorite-menu.styles';

const MENU_ITEMS = [
  {
    id: '1',
    icon: require('@/shared/assets/icons/medium-icon2.svg'),
    label: '송금',
  },
  {
    id: '2',
    icon: require('@/shared/assets/icons/medium-icon1.svg'),
    label: '리포트',
  },
  {
    id: '3',
    icon: require('@/shared/assets/icons/medium-icon3.svg'),
    label: '각서',
  },
  {
    id: '4',
    icon: require('@/shared/assets/icons/medium-icon4.svg'),
    label: '메세지 설정',
  },
];

interface FavoriteMenuProps {
  onItemPress?: (id: string) => void;
}

export function FavoriteMenu({ onItemPress }: FavoriteMenuProps) {
  return (
    <View style={styles.card}>
      <View style={styles.row}>
        {MENU_ITEMS.map((item) => (
          <TouchableOpacity
            key={item.id}
            style={styles.item}
            onPress={() => onItemPress?.(item.id)}
          >
            <Image source={item.icon} style={{ width: 54, height: 54 }} />
            <Text style={styles.label}>{item.label}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </View>
  );
}
