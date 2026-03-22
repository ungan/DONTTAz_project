import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  BackHandler,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useFocusEffect } from '@react-navigation/native';
import { useSessionStore } from '@/entities/session';
import { useMemorandumForm, SignStampOverlay } from '@/features/vault-memorandum';
import { styles } from './vault-memorandum-page.styles';

export function VaultMemorandumPage() {
  const router = useRouter();
  const { name } = useSessionStore();
  const { content1, setContent1, content2, setContent2, content3, setContent3, isValid } =
    useMemorandumForm();
  const [showStamp, setShowStamp] = useState(false);

  // Android 물리 백버튼 차단
  useFocusEffect(
    React.useCallback(() => {
      const sub = BackHandler.addEventListener('hardwareBackPress', () => true);
      return () => sub.remove();
    }, [])
  );

  const today = new Date();
  const dateStr = `${today.getFullYear()}년 ${String(today.getMonth() + 1).padStart(2, '0')}월 ${String(today.getDate()).padStart(2, '0')}일`;

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.flex}
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        keyboardVerticalOffset={Platform.OS === 'ios' ? 0 : 20}
      >
        <ScrollView
          style={styles.scroll}
          contentContainerStyle={styles.scrollContent}
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
        >
          {/* 제목 */}
          <Text style={styles.mainTitle}>각  서</Text>

          {/* 작성자 정보 */}
          <View style={styles.userInfoSection}>
            <Text style={styles.userInfoText}>작성자  {name ?? '---'}</Text>
            <Text style={styles.userInfoText}>작성일  {dateStr}</Text>
          </View>

          <View style={styles.divider} />

          {/* 서약 도입부 */}
          <Text style={styles.sectionSubtitle}>
            본인은 목표 달성을 위해 아래 사항을 성실히 이행할 것을 서약하며,{'\n'}
            불이행 시 이를 주변에 공개하는 것에 동의합니다.
          </Text>

          {/* 제1조 */}
          <View style={styles.articleBlock}>
            <Text style={styles.articleTitle}>제1조 (불이행 사유)</Text>
            <Text style={styles.articleText}>만약 내가 목표를 달성하지 못한다면,</Text>
            <View style={styles.article1Row}>
              <Text style={styles.articleText}>그 원인은 </Text>
              <TextInput
                style={styles.causeInput}
                value={content1}
                onChangeText={setContent1}
                placeholder="직접 입력"
                placeholderTextColor="#AAAAAA"
                maxLength={30}
              />
              <Text style={styles.articleText}> 때문일 것이다.</Text>
            </View>
          </View>

          {/* 제2조 */}
          <View style={styles.articleBlock}>
            <Text style={styles.articleTitle}>제2조 (주변 사람 공표 사항)</Text>
            <TextInput
              style={styles.multilineInput}
              value={content2}
              onChangeText={setContent2}
              multiline
              textAlignVertical="top"
            />
          </View>

          {/* 제3조 */}
          <View style={styles.articleBlock}>
            <Text style={styles.articleTitle}>제3조 (향후 다짐)</Text>
            <TextInput
              style={styles.multilineInput}
              value={content3}
              onChangeText={setContent3}
              multiline
              textAlignVertical="top"
            />
          </View>

          {/* 서명 */}
          <View style={styles.signatureSection}>
            <Text style={styles.signatureName}>서약자  {name ?? '---'}  (인)</Text>
          </View>

          {/* 서명 버튼 */}
          <TouchableOpacity
            style={[styles.signButton, !isValid && styles.signButtonDisabled]}
            onPress={() => setShowStamp(true)}
            disabled={!isValid}
            activeOpacity={0.85}
          >
            <Text style={styles.signButtonText}>돌이킬 수 없는 서명하기</Text>
          </TouchableOpacity>

          <View style={{ height: 32 }} />
        </ScrollView>
      </KeyboardAvoidingView>

      <SignStampOverlay
        visible={showStamp}
        onAnimationEnd={() => router.replace('/vault-webhook')}
      />
    </SafeAreaView>
  );
}
