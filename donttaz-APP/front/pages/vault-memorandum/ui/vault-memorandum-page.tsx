import React, { useState } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  BackHandler,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { useFocusEffect } from '@react-navigation/native';
import { useSessionStore } from '@/entities/session';
import { signMemorandumApi } from '@/entities/vault';
import { SignStampOverlay } from '@/features/vault-memorandum';
import { styles } from './vault-memorandum-page.styles';

const ARTICLES: { title: string; prefix: string; emphasis: string }[] = [
  {
    title: '제1조 (긴급 출금)',
    prefix: '목표 금액 달성 전 긴급 출금 시도 시, 사전 지정한 지인에게 ',
    emphasis: '수치심 메시지가 즉시 발송된다.',
  },
  {
    title: '제2조 (중도 해지)',
    prefix: '위시리스트 금고를 목표 달성 전 해지할 경우, 사전 지정한 지인에게 ',
    emphasis: '수치심 메시지가 즉시 발송된다.',
  },
  {
    title: '제3조 (유효 기간)',
    prefix: '본 각서는 위시리스트 목표 금액 달성 시까지 유효하며, 달성과 동시에 ',
    emphasis: '자동 소멸된다.',
  },
];

export function VaultMemorandumPage() {
  const router = useRouter();
  const { vaultId, readonly } = useLocalSearchParams<{ vaultId: string; readonly?: string }>();
  const isReadonly = readonly === 'true';
  const { name } = useSessionStore();
  const [showStamp, setShowStamp] = useState(false);

  const handleSign = async () => {
    const id = Number(vaultId);
    if (!vaultId || isNaN(id)) return;
    try {
      await signMemorandumApi(id);
      setShowStamp(true);
    } catch {
    }
  };

  useFocusEffect(
    React.useCallback(() => {
      if (isReadonly) return;
      const sub = BackHandler.addEventListener('hardwareBackPress', () => true);
      return () => sub.remove();
    }, [isReadonly])
  );

  const today = new Date();
  const dateStr = `${today.getFullYear()}년 ${String(today.getMonth() + 1).padStart(2, '0')}월 ${String(today.getDate()).padStart(2, '0')}일`;

  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView
        style={styles.scroll}
        contentContainerStyle={styles.scrollContent}
        showsVerticalScrollIndicator={false}
      >
        <Text style={styles.mainTitle}>각  서</Text>

        <View style={styles.userInfoSection}>
          <Text style={styles.userInfoText}>작성자  {name ?? '---'}</Text>
          <Text style={styles.userInfoText}>작성일  {dateStr}</Text>
        </View>

        <View style={styles.divider} />

        <Text style={styles.sectionSubtitle}>
          나는 아래 내용을 숙지하고 이에 동의한다.
        </Text>

        {ARTICLES.map((article) => (
          <View key={article.title} style={styles.articleBlock}>
            <Text style={styles.articleTitle}>{article.title}</Text>
            <Text style={styles.articleText}>
              {article.prefix}
              <Text style={styles.articleEmphasis}>{article.emphasis}</Text>
            </Text>
          </View>
        ))}

        <View style={styles.signatureSection}>
          <Text style={styles.signatureName}>서약자  {name ?? '---'}</Text>
        </View>

      </ScrollView>

      <LinearGradient
        colors={['rgba(250,250,247,0)', 'rgba(250,250,247,1)']}
        style={styles.fadeOverlay}
        pointerEvents="none"
      />

      {isReadonly ? (
        <TouchableOpacity
          style={styles.signButton}
          onPress={() => router.back()}
          activeOpacity={0.85}
        >
          <Text style={styles.signButtonText}>뒤로가기</Text>
        </TouchableOpacity>
      ) : (
        <TouchableOpacity
          style={styles.signButton}
          onPress={handleSign}
          activeOpacity={0.85}
        >
          <Text style={styles.signButtonText}>돌이킬 수 없는 서명하기</Text>
        </TouchableOpacity>
      )}

      <SignStampOverlay
        visible={showStamp}
        onAnimationEnd={() => router.replace(`/vault-webhook?vaultId=${vaultId}`)}
      />
    </SafeAreaView>
  );
}
