import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { styles } from './page-header.styles';

interface PageHeaderProps {
  title: string;
  onBack?: () => void;
  rightAction?: React.ReactNode;
}

export function PageHeader({ title, onBack, rightAction }: PageHeaderProps) {
  const router = useRouter();
  const insets = useSafeAreaInsets();

  const handleBack = () => {
    if (onBack) {
      onBack();
    } else {
      router.back();
    }
  };

  return (
    <View style={[styles.container, { paddingTop: insets.top + 12 }]}>
      <TouchableOpacity style={styles.backButton} onPress={handleBack} activeOpacity={0.7}>
        <Ionicons name="chevron-back" size={20} color="#374151" />
      </TouchableOpacity>
      <Text style={styles.title}>{title}</Text>
      {rightAction ? <View style={styles.rightAction}>{rightAction}</View> : <View style={styles.placeholder} />}
    </View>
  );
}
