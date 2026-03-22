import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, TextInput, BackHandler } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { PageHeader, Button } from '@/shared/ui';
import { useWebhookForm, WEBHOOK_MESSAGE_MAX_LENGTH } from '@/features/vault-webhook';
import type { WebhookPlatform } from '@/entities/vault';
import { styles } from './vault-webhook-page.styles';

const PLATFORMS: { id: WebhookPlatform; label: string }[] = [
  { id: 'MATTERMOST', label: 'Mattermost' },
  { id: 'KAKAO_TALK', label: 'KakaoTalk' },
];

export function VaultWebhookPage() {
  const router = useRouter();
  const { platform, setPlatform, message, setMessage, isValid } = useWebhookForm();

  const isOver = message.length > WEBHOOK_MESSAGE_MAX_LENGTH;

  // Android 물리 백버튼 차단
  useFocusEffect(
    React.useCallback(() => {
      const sub = BackHandler.addEventListener('hardwareBackPress', () => true);
      return () => sub.remove();
    }, [])
  );

  const handleSave = () => {
    router.replace('/(tabs)/vault');
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <PageHeader title="수치심 메세지 설정" onBack={() => {}} />
      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        {/* 상단 타이틀 */}
        <View style={styles.heroSection}>
          <Text style={styles.heroTitle}>목표를 지키는{'\n'}가장 강력한 방법</Text>
          <Text style={styles.heroText}>
            중도 인출 시 등록된 플랫폼으로 메시지가 자동 발송됩니다.{'\n'}
            부끄러움이 클수록, 포기하기 더 어려워집니다.
          </Text>
        </View>

        {/* 플랫폼 선택 카드 */}
        <View style={styles.card}>
          <View style={styles.sectionHeader}>
            <View style={styles.sectionIconBox}>
              <Ionicons name="share-social-outline" size={15} color="#3B82F6" />
            </View>
            <Text style={styles.sectionTitle}>웹훅 등록 플랫폼 선택</Text>
          </View>

          {PLATFORMS.map(({ id, label }) => {
            const selected = platform === id;
            return (
              <TouchableOpacity
                key={id}
                style={[styles.platformCard, selected && styles.platformCardSelected]}
                onPress={() => setPlatform(id)}
                activeOpacity={0.7}
              >
                <View style={[styles.platformIcon, id === 'MATTERMOST' ? styles.mattermostIcon : styles.kakaoIcon]}>
                  <Text style={[styles.platformIconText, id === 'KAKAO_TALK' && styles.kakaoIconText]}>
                    {id === 'MATTERMOST' ? 'M' : '💬'}
                  </Text>
                </View>
                <Text style={styles.platformName}>{label}</Text>
                <View style={[styles.radioOuter, selected && styles.radioOuterSelected]}>
                  {selected && <View style={styles.radioInner} />}
                </View>
              </TouchableOpacity>
            );
          })}
        </View>

        {/* 메세지 편집 카드 */}
        <View style={styles.card}>
          <View style={styles.sectionHeader}>
            <View style={styles.sectionIconBox}>
              <Ionicons name="mail-outline" size={15} color="#3B82F6" />
            </View>
            <Text style={styles.sectionTitle}>수치심 메세지 편집</Text>
          </View>

          <Text style={styles.sectionSubtitle}>
            예산을 초과하면 등록된 플랫폼으로 다음 메시지를 발송합니다.
          </Text>

          <View style={styles.messageInputWrapper}>
            <TextInput
              style={styles.messageInput}
              value={message}
              onChangeText={setMessage}
              placeholder="예: 나는 의지박약입니다. 계획한 예산도 못 지키는 사람입니다..."
              placeholderTextColor="#9CA3AF"
              multiline
              maxLength={WEBHOOK_MESSAGE_MAX_LENGTH + 10}
            />
            <Text style={[styles.messageCounter, isOver && styles.messageCounterOver]}>
              {message.length} / {WEBHOOK_MESSAGE_MAX_LENGTH}
            </Text>
          </View>

          <View style={styles.infoBox}>
            <Ionicons name="information-circle-outline" size={15} color="#0EA5E9" />
            <Text style={styles.infoText}>
              메시지는 중도 인출 시 즉시 발송되며, 취소할 수 없습니다. 신중하게 작성해 주세요.
            </Text>
          </View>
        </View>

        {/* 저장 버튼 */}
        <View style={styles.buttonWrapper}>
          <Button
            label="설정 저장하기"
            disabled={!isValid || isOver}
            onPress={handleSave}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
