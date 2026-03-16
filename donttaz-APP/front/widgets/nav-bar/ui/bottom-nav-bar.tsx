import { Ionicons } from '@expo/vector-icons';
import { BottomTabBarProps } from '@react-navigation/bottom-tabs';
import { Pressable, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Primary } from '@/shared/constants/theme';
import { styles } from './bottom-nav-bar.styles';

// 탭 라우트명 - 아이콘 - 레이블 매핑 상수
const TAB_CONFIG = [
  { name: 'home', icon: 'home-outline' as const, label: '홈' },
  { name: 'plan', icon: 'bar-chart-outline' as const, label: '플랜' },
  { name: 'vault', icon: 'archive-outline' as const, label: '금고' },
  { name: 'menu', icon: 'menu-outline' as const, label: '전체' },
];

export function BottomNavBar({ state, descriptors, navigation }: BottomTabBarProps) {
  // 하단 제스처 바 높이 확보
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.container, { paddingBottom: insets.bottom + 8 }]}>
      {state.routes.map((route, index) => {
        const tabConfig = TAB_CONFIG.find((t) => t.name === route.name);
        if (!tabConfig) return null;

        // 현재 활성 탭 여부
        const isFocused = state.index === index;
        const { options } = descriptors[route.key];

        // React Navigation 이벤트 시스템을 통한 탭 이동
        const onPress = () => {
          const event = navigation.emit({
            type: 'tabPress',
            target: route.key,
            canPreventDefault: true,
          });
          if (!isFocused && !event.defaultPrevented) {
            navigation.navigate(route.name);
          }
        };

        return (
          <Pressable
            key={route.key}
            accessibilityRole="button"
            accessibilityState={isFocused ? { selected: true } : {}}
            accessibilityLabel={options.tabBarAccessibilityLabel}
            onPress={onPress}
            style={styles.tab}
          >
            <Ionicons
              name={tabConfig.icon}
              size={24}
              color={isFocused ? Primary : '#9BA1A6'}
            />
            <Text style={[styles.label, isFocused && styles.labelFocused]}>
              {tabConfig.label}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}
