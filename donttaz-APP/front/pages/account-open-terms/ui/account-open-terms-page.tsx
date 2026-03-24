import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Button, PageHeader } from '@/shared/ui';
import {
  AccountOpenProgress,
  AccountOpenTermsSection,
  useAccountOpenFlow,
} from '@/features/account-open';
import { styles } from './account-open-terms-page.styles';

export function AccountOpenTermsPage() {
  const insets = useSafeAreaInsets();
  const {
    currentStep,
    errorMessage,
    isProductLoading,
    requiredTerms,
    optionalTerms,
    isRequiredTermsAgreed,
    isOptionalTermsAgreed,
    startFlow,
    toggleTerm,
    toggleRequiredTerms,
    toggleOptionalTerms,
    submitTermsStep,
  } = useAccountOpenFlow();

  useEffect(() => {
    startFlow();
  }, []);

  return (
    <View style={styles.outerContainer}>
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.headerBlock}>
        <PageHeader title="약관 동의" />
        <AccountOpenProgress currentStep={currentStep} />
      </View>

      <View style={styles.heroSection}>
        <Text style={styles.heroTitle}>서비스 이용약관 동의</Text>
      </View>

      <View style={styles.termsGroup}>
        <AccountOpenTermsSection
          title="필수 약관"
          subtitle="계좌 개설 및 전자금융 신청에 반드시 필요합니다."
          allAgreed={isRequiredTermsAgreed}
          items={requiredTerms}
          onToggleAll={toggleRequiredTerms}
          onToggleItem={toggleTerm}
        />

        <AccountOpenTermsSection
          title="선택 약관"
          subtitle="동의하지 않아도 계좌 개설이 가능합니다."
          allAgreed={isOptionalTermsAgreed}
          items={optionalTerms}
          onToggleAll={toggleOptionalTerms}
          onToggleItem={toggleTerm}
        />
      </View>

    </ScrollView>

    <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}
      <Button
        label={isProductLoading ? '불러오는 중...' : '다음'}
        disabled={!isRequiredTermsAgreed || isProductLoading}
        onPress={submitTermsStep}
      />
    </View>
    </View>
  );
}
