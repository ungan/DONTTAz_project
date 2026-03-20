import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { Button, PageHeader } from '@/shared/ui';
import {
  AccountOpenProgress,
  AccountOpenTermsSection,
  useAccountOpenFlow,
} from '@/features/account-open';
import { styles } from './account-open-terms-page.styles';

export function AccountOpenTermsPage() {
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
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="계좌정보 입력" />

      <AccountOpenProgress currentStep={currentStep} />

      <View style={styles.heroSection}>
        <Text style={styles.heroTitle}>통장 생성을 위한 약관에 동의해주세요</Text>
        <Text style={styles.heroDescription}>
          필수 약관은 계좌 생성 진행에 필요하고, 선택 항목은 알림과 부가 서비스 설정에
          사용됩니다.
        </Text>
        {isProductLoading ? (
          <Text style={styles.heroDescription}>계좌 상품 정보를 불러오는 중입니다...</Text>
        ) : null}
      </View>

      <AccountOpenTermsSection
        title="필수 약관"
        subtitle="계좌 개설과 전자금융 신청에 필요한 항목입니다."
        allAgreed={isRequiredTermsAgreed}
        items={requiredTerms}
        onToggleAll={toggleRequiredTerms}
        onToggleItem={toggleTerm}
      />

      <AccountOpenTermsSection
        title="선택 약관"
        subtitle="추후 알림 서비스 및 안내 메시지 수신에 사용됩니다."
        allAgreed={isOptionalTermsAgreed}
        items={optionalTerms}
        onToggleAll={toggleOptionalTerms}
        onToggleItem={toggleTerm}
      />

      {errorMessage ? <Text style={styles.errorText}>{errorMessage}</Text> : null}

      <Button
        label={isProductLoading ? '불러오는 중...' : '다음'}
        disabled={!isRequiredTermsAgreed || isProductLoading}
        onPress={submitTermsStep}
      />
    </ScrollView>
  );
}
