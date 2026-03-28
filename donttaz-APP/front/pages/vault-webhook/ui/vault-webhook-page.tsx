import React, { useEffect, useState } from 'react';
import { Alert, View, Text, ScrollView, TextInput, TouchableOpacity, BackHandler, Image, KeyboardAvoidingView, type ImageSourcePropType } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { useFocusEffect } from '@react-navigation/native';
import { Ionicons } from '@expo/vector-icons';
import { PageHeader, Button } from '@/shared/ui';
import { useWebhookForm, WEBHOOK_MESSAGE_MAX_LENGTH } from '@/features/vault-webhook';
import { postPenaltyWebhookApi, getPenaltyWebhookApi, getVaultApi } from '@/entities/vault';
import type { PlatformType } from '@/entities/vault';
import { styles } from './vault-webhook-page.styles';

const mattermostLogo = require('@/shared/assets/logo/mattermost-logo.png');
const discordLogo = require('@/shared/assets/logo/discord-logo.jpg');

const PLATFORMS: { id: PlatformType; label: string; color: string; bg: string; logo: ImageSourcePropType }[] = [
  { id: 'MATTERMOST', label: 'Mattermost', color: '#1B5299', bg: '#E8EEF7', logo: mattermostLogo },
  { id: 'DISCORD',    label: 'Discord',    color: '#5865F2', bg: '#EEEEFF', logo: discordLogo },
];

export function VaultWebhookPage() {
  const router = useRouter();
  const { vaultId, from } = useLocalSearchParams<{ vaultId: string; from: string }>();
  const isSetupFlow = from === 'setup';
  const { platform, setPlatform, message, setMessage, webhookUrl, setWebhookUrl, isValid } = useWebhookForm();
  const [isEditMode, setIsEditMode] = useState(false);

  const isOver = message.length > WEBHOOK_MESSAGE_MAX_LENGTH;

  useFocusEffect(
    React.useCallback(() => {
      if (!isSetupFlow) return;
      const sub = BackHandler.addEventListener('hardwareBackPress', () => true);
      return () => sub.remove();
    }, [isSetupFlow])
  );

  useEffect(() => {
    const loadExisting = async () => {
      const id = vaultId && !isNaN(Number(vaultId)) ? Number(vaultId) : null;
      if (!id) return;
      const existing = await getPenaltyWebhookApi(id);
      if (existing?.shameMessage) {
        setPlatform(existing.platformType);
        setMessage(existing.shameMessage);
        setWebhookUrl(existing.webhookUrl ?? '');
        setIsEditMode(true);
      }
    };
    void loadExisting();
  }, []);

  const resolveVaultId = async (): Promise<number | null> => {
    if (vaultId && !isNaN(Number(vaultId))) return Number(vaultId);
    try {
      const vaults = await getVaultApi();
      const wishlist = vaults.vaults.find((v) => v.vaultKind === 'WISHLIST');
      return wishlist?.vaultId ?? null;
    } catch {
      return null;
    }
  };

  const handleSave = async () => {
    console.log('[WebhookSave] handleSave called', { platform, message });
    const id = await resolveVaultId();
    console.log('[WebhookSave] resolved vaultId', id);
    if (!id) {
      Alert.alert(
        '위시금고 필요',
        '수치심 메세지를 설정하려면 먼저 위시금고를 만들어야 합니다.',
        [
          {
            text: '위시금고 만들기',
            onPress: () => router.replace('/(tabs)/vault?tab=wishlist'),
          },
        ]
      );
      return;
    }
    try {
      await postPenaltyWebhookApi(id, {
        platformType: platform,
        shameMessage: message,
        ...(webhookUrl.trim() && { webhookUrl: webhookUrl.trim() }),
      });
      console.log('[WebhookSave] success, navigating');
      router.back();
    } catch (e) {
      console.log('[WebhookSave] error', e);
    }
  };

  return (
    <SafeAreaView edges={['bottom']} style={styles.safeArea}>
      <KeyboardAvoidingView behavior="padding" style={{ flex: 1 }}>
      <PageHeader title="수치심 메세지 설정" onBack={isSetupFlow ? () => {} : undefined} />

      <ScrollView
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
        keyboardShouldPersistTaps="handled"
      >
        <View style={styles.heroSection}>
          <Text style={styles.heroTitle}>친구에게 메세지 전송</Text>
          <Text style={styles.heroText}>
            위시리스트 긴급 출금·중도 해지 시 전송됩니다
          </Text>
        </View>

        {/* 플랫폼 선택 */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>발송 채널</Text>
          <View style={styles.platformGrid}>
            {PLATFORMS.map((p) => {
              const selected = platform === p.id;
              return (
                <TouchableOpacity
                  key={p.id}
                  style={[styles.platformCard, selected && styles.platformCardSelected, { gap: p.id === 'MATTERMOST' ? 4 : 9 }]}
                  onPress={() => setPlatform(p.id)}
                  activeOpacity={0.7}
                >
                  <Image source={p.logo} style={{ width: p.id === 'MATTERMOST' ? 48 : 40, height: p.id === 'MATTERMOST' ? 48 : 40, borderRadius: 4, marginTop: p.id === 'DISCORD' ? 2 : 0 }} resizeMode="contain" />
                  <Text style={[styles.platformLabel, selected && styles.platformLabelSelected]}>
                    {p.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>
        </View>

        {/* Webhook URL */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>Webhook URL (선택)</Text>
          <View style={styles.urlInputWrapper}>
            <TextInput
              style={styles.urlInput}
              value={webhookUrl}
              onChangeText={setWebhookUrl}
              placeholder="https://discord.gg/..."
              placeholderTextColor="#9CA3AF"
              autoCapitalize="none"
              autoCorrect={false}
              keyboardType="url"
            />
          </View>
          <View style={styles.infoBox}>
            <Ionicons name="information-circle-outline" size={14} color="#9CA3AF" />
            <Text style={styles.infoText}>입력하지 않으면 기본 Webhook 설정이 사용됩니다.</Text>
          </View>
        </View>

        {/* 메세지 */}
        <View style={styles.card}>
          <Text style={styles.cardLabel}>전송 메세지</Text>
          <View style={styles.messageInputWrapper}>
            <TextInput
              style={styles.messageInput}
              value={message}
              onChangeText={setMessage}
              placeholder={"예: 저는 지금 통장을 배신했습니다. 한마디 부탁드립니다."}
              placeholderTextColor="#9CA3AF"
              multiline
              maxLength={WEBHOOK_MESSAGE_MAX_LENGTH + 10}
            />
            <Text style={[styles.messageCounter, isOver && styles.messageCounterOver]}>
              {message.length} / {WEBHOOK_MESSAGE_MAX_LENGTH}
            </Text>
          </View>
          <View style={styles.infoBox}>
            <Ionicons name="information-circle-outline" size={14} color="#9CA3AF" />
            <Text style={styles.infoText}>메시지는 향후 수정 가능합니다.</Text>
          </View>
        </View>
      </ScrollView>

      <View style={styles.buttonWrapper}>
        <Button
          label={isEditMode ? '수정하기' : '저장하기'}
          disabled={!isValid || isOver}
          onPress={handleSave}
        />
      </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
