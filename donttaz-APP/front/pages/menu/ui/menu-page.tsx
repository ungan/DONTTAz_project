import { useState } from 'react';
import { ScrollView, Text, View, TouchableOpacity } from 'react-native';
import { useRouter } from 'expo-router';
import { Ionicons } from '@expo/vector-icons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useSessionStore } from '@/entities/session';
import { useLogout } from '@/features/auth';
import { ConfirmModal } from '@/shared/ui';
import { styles } from './menu-page.styles';

type MenuItem = {
  icon: keyof typeof Ionicons.glyphMap;
  label: string;
  description: string;
  onPress: () => void;
  danger?: boolean;
};

export function MenuPage() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { name } = useSessionStore();
  const { logout, isPending, error, resetError } = useLogout();
  const [isConfirmVisible, setIsConfirmVisible] = useState(false);

  const openConfirm = () => {
    resetError();
    setIsConfirmVisible(true);
  };

  const closeConfirm = () => {
    setIsConfirmVisible(false);
  };

  const handleConfirmLogout = async () => {
    const isSuccess = await logout();
    if (isSuccess) {
      setIsConfirmVisible(false);
    }
  };

  const financeItems: MenuItem[] = [
    {
      icon: 'receipt-outline',
      label: '전체 결제내역',
      description: '내 계좌 거래내역 조회',
      onPress: () => router.push('/payment-history'),
    },
    {
      icon: 'swap-horizontal-outline',
      label: '이체',
      description: '다른 계좌로 송금하기',
      onPress: () => router.push('/transfer-account'),
    },
    {
      icon: 'wallet-outline',
      label: '계좌 개설',
      description: '새 입출금 통장 만들기',
      onPress: () => router.push('/account-open-terms'),
    },
  ];

  const planItems: MenuItem[] = [
    {
      icon: 'bar-chart-outline',
      label: '소비 리포트',
      description: '월별·연별 소비 분석',
      onPress: () => router.push('/report'),
    },
    {
      icon: 'trending-down-outline',
      label: '소비 계획',
      description: '번다운 차트로 예산 관리',
      onPress: () => router.push('/(tabs)/plan'),
    },
  ];

  const vaultItems: MenuItem[] = [
    {
      icon: 'lock-closed-outline',
      label: '가상금고',
      description: '목돈 모으기 & 위시리스트',
      onPress: () => router.push('/(tabs)/vault'),
    },
    {
      icon: 'document-text-outline',
      label: '각서 작성',
      description: '소비 절제 각서 설정',
      onPress: () => router.push('/vault-memorandum'),
    },
  ];

  const accountItems: MenuItem[] = [
    {
      icon: 'log-out-outline',
      label: '로그아웃',
      description: '이 기기에서 세션 종료',
      onPress: openConfirm,
      danger: true,
    },
  ];

  const renderItem = (item: MenuItem) => (
    <TouchableOpacity
      key={item.label}
      style={styles.menuItem}
      onPress={item.onPress}
      activeOpacity={0.6}
    >
      <View style={[styles.iconWrapper, item.danger && styles.iconWrapperDanger]}>
        <Ionicons
          name={item.icon}
          size={22}
          color={item.danger ? '#DC2626' : '#5899FD'}
        />
      </View>
      <View style={styles.menuTextGroup}>
        <Text style={[styles.menuLabel, item.danger && styles.menuLabelDanger]}>
          {item.label}
        </Text>
        <Text style={styles.menuDescription}>{item.description}</Text>
      </View>
      <Ionicons name="chevron-forward" size={18} color="#D1D5DB" />
    </TouchableOpacity>
  );

  return (
    <>
      <ScrollView
        contentContainerStyle={[styles.container, { paddingTop: insets.top + 16 }]}
        showsVerticalScrollIndicator={false}
      >
        <View style={styles.profileSection}>
          <View>
            <Text style={styles.profileName}>{name ?? '사용자'}님</Text>
            <Text style={styles.profileSub}>내 정보 관리</Text>
          </View>
          <Ionicons name="chevron-forward" size={20} color="#9CA3AF" />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionHeader}>금융</Text>
          <View style={styles.sectionCard}>
            {financeItems.map(renderItem)}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionHeader}>소비 관리</Text>
          <View style={styles.sectionCard}>
            {planItems.map(renderItem)}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionHeader}>가상금고</Text>
          <View style={styles.sectionCard}>
            {vaultItems.map(renderItem)}
          </View>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionHeader}>계정</Text>
          <View style={styles.sectionCard}>
            {accountItems.map(renderItem)}
          </View>
        </View>

        {error ? <Text style={styles.errorText}>{error}</Text> : null}
      </ScrollView>

      <ConfirmModal
        visible={isConfirmVisible}
        title="로그아웃할까요?"
        message="현재 기기에 저장된 로그인 정보와 간편 PIN 정보를 정리합니다."
        confirmLabel={isPending ? '처리 중...' : '로그아웃'}
        cancelLabel="취소"
        onConfirm={handleConfirmLogout}
        onCancel={isPending ? () => undefined : closeConfirm}
      />
    </>
  );
}
