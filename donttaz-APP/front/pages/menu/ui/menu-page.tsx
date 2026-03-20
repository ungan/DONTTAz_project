import { useState } from 'react';
import { ScrollView, Text, View } from 'react-native';
import { useRouter } from 'expo-router';
import { useSessionStore } from '@/entities/session';
import { useLogout } from '@/features/auth';
import { Button, ConfirmModal } from '@/shared/ui';
import { styles } from './menu-page.styles';

export function MenuPage() {
  const router = useRouter();
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

  return (
    <>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.content}>
          <View style={styles.heroCard}>
            <Text style={styles.heroLabel}>전체</Text>
            <Text style={styles.heroTitle}>{name ? `${name}님 안녕하세요` : '계정 설정'}</Text>
            <Text style={styles.heroDescription}>
              자주 쓰는 계좌 개설과 로그인 관련 작업을 이 화면에서 빠르게 시작할 수 있습니다.
            </Text>
          </View>

          <View style={styles.sectionCard}>
            <Text style={styles.sectionLabel}>신규 계좌</Text>
            <Text style={styles.sectionTitle}>통장 개설 시작</Text>
            <Text style={styles.sectionText}>
              약관 동의 단계부터 계좌 개설 플로우를 확인할 수 있습니다. 현재는 약관 화면과
              다음 단계 자리까지만 연결되어 있습니다.
            </Text>
            <Button label="계좌 개설" onPress={() => router.push('/account-open-terms')} />
          </View>

          <View style={styles.sectionCard}>
            <Text style={styles.sectionLabel}>로그인 상태</Text>
            <Text style={styles.sectionTitle}>이 기기에서 세션 종료</Text>
            <Text style={styles.sectionText}>
              로그아웃하면 access token, refresh token, 저장된 PIN 정보를 함께 정리하고 로그인
              화면으로 이동합니다.
            </Text>
            {error ? <Text style={styles.errorText}>{error}</Text> : null}
          </View>
        </View>

        <View style={styles.bottomSection}>
          <Text style={styles.helperText}>
            금융 서비스 특성상 공용 기기나 테스트 기기에서는 사용 후 로그아웃을 권장합니다.
          </Text>
          <Button label="로그아웃" variant="outline" onPress={openConfirm} />
        </View>
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
