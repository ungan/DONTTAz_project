import React, { useEffect } from 'react';
import { ScrollView, View, Text, KeyboardAvoidingView } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Button, PageHeader } from '@/shared/ui';
import {
  AccountOpenProfileForm,
  AccountOpenProgress,
  useAccountOpenFlow,
} from '@/features/account-open';
import { styles } from './account-open-profile-page.styles';

export function AccountOpenProfilePage() {
  const insets = useSafeAreaInsets();
  const {
    customerName,
    phoneNumber,
    verifiedOwnerName,
    verifiedOwnerPhoneNumber,
    zipCode,
    addressDetail,
    customerNameError,
    phoneNumberError,
    addressError,
    isSmsAgreed,
    isAlimtalkAgreed,
    isMailAgreed,
    isRequiredTermsAgreed,
    moveToProfileStep,
    updateCustomerName,
    updatePhoneNumber,
    updateZipCode,
    updateAddressDetail,
    toggleSmsAgreed,
    toggleAlimtalkAgreed,
    toggleMailAgreed,
    submitProfileStep,
  } = useAccountOpenFlow();

  useEffect(() => {
    if (!isRequiredTermsAgreed) {
      router.replace('/account-open-terms' as never);
      return;
    }

    if (!verifiedOwnerName || !verifiedOwnerPhoneNumber) {
      router.replace('/account-open-verify' as never);
      return;
    }

    moveToProfileStep();
  }, [isRequiredTermsAgreed, verifiedOwnerName, verifiedOwnerPhoneNumber]);

  return (
    <View style={styles.outerContainer}>
      <View style={styles.headerBlock}>
        <PageHeader title="개인정보 입력" onBack={() => router.replace('/account-open-verify' as never)} />
        <AccountOpenProgress currentStep="profile" />
      </View>

      <KeyboardAvoidingView behavior="padding" style={{ flex: 1 }}>
      <ScrollView contentContainerStyle={styles.container}>
        <View style={styles.heroSection}>
          <Text style={styles.title}>개인정보 입력</Text>
          <Text style={styles.description}>
            이름과 휴대전화번호는 본인인증 결과로 자동 입력됩니다.
          </Text>
        </View>

        <AccountOpenProfileForm
          customerName={customerName}
          phoneNumber={phoneNumber}
          zipCode={zipCode}
          addressDetail={addressDetail}
          customerNameError={customerNameError}
          phoneNumberError={phoneNumberError}
          addressError={addressError}
          isSmsAgreed={isSmsAgreed}
          isAlimtalkAgreed={isAlimtalkAgreed}
          isMailAgreed={isMailAgreed}
          onCustomerNameChange={updateCustomerName}
          onPhoneNumberChange={updatePhoneNumber}
          onZipCodeChange={updateZipCode}
          onAddressDetailChange={updateAddressDetail}
          onToggleSmsAgreed={toggleSmsAgreed}
          onToggleAlimtalkAgreed={toggleAlimtalkAgreed}
          onToggleMailAgreed={toggleMailAgreed}
        />
      </ScrollView>

      <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
        <Button label="다음" onPress={submitProfileStep} />
      </View>
      </KeyboardAvoidingView>
    </View>
  );
}
