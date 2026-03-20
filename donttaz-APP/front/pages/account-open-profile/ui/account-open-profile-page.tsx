import React, { useEffect } from 'react';
import { ScrollView, View, Text } from 'react-native';
import { router } from 'expo-router';
import { Button, PageHeader } from '@/shared/ui';
import {
  AccountOpenProfileForm,
  AccountOpenProgress,
  useAccountOpenFlow,
} from '@/features/account-open';
import { styles } from './account-open-profile-page.styles';

export function AccountOpenProfilePage() {
  const {
    customerName,
    englishName,
    phoneNumber,
    verifiedOwnerName,
    verifiedOwnerPhoneNumber,
    zipCode,
    address,
    addressDetail,
    emailLocalPart,
    emailDomain,
    customerNameError,
    englishNameError,
    phoneNumberError,
    addressError,
    emailError,
    isSmsAgreed,
    isAlimtalkAgreed,
    isMailAgreed,
    isRequiredTermsAgreed,
    moveToProfileStep,
    updateCustomerName,
    updateEnglishName,
    updatePhoneNumber,
    updateZipCode,
    updateAddress,
    updateAddressDetail,
    updateEmailLocalPart,
    updateEmailDomain,
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
    <ScrollView contentContainerStyle={styles.container}>
      <PageHeader title="계좌정보 입력" onBack={() => router.replace('/account-open-verify' as never)} />

      <AccountOpenProgress currentStep="profile" />

      <View style={styles.heroSection}>
        <Text style={styles.title}>본인인증 정보를 확인해 주세요.</Text>
        <Text style={styles.description}>
          이름과 휴대전화번호는 본인인증 결과로 자동 입력되며, 나머지 정보를 이어서 입력합니다.
        </Text>
      </View>

      <AccountOpenProfileForm
        customerName={customerName}
        englishName={englishName}
        phoneNumber={phoneNumber}
        zipCode={zipCode}
        address={address}
        addressDetail={addressDetail}
        emailLocalPart={emailLocalPart}
        emailDomain={emailDomain}
        customerNameError={customerNameError}
        englishNameError={englishNameError}
        phoneNumberError={phoneNumberError}
        addressError={addressError}
        emailError={emailError}
        isSmsAgreed={isSmsAgreed}
        isAlimtalkAgreed={isAlimtalkAgreed}
        isMailAgreed={isMailAgreed}
        onCustomerNameChange={updateCustomerName}
        onEnglishNameChange={updateEnglishName}
        onPhoneNumberChange={updatePhoneNumber}
        onZipCodeChange={updateZipCode}
        onAddressChange={updateAddress}
        onAddressDetailChange={updateAddressDetail}
        onEmailLocalPartChange={updateEmailLocalPart}
        onEmailDomainChange={updateEmailDomain}
        onToggleSmsAgreed={toggleSmsAgreed}
        onToggleAlimtalkAgreed={toggleAlimtalkAgreed}
        onToggleMailAgreed={toggleMailAgreed}
      />

      <Button label="다음" onPress={submitProfileStep} />
    </ScrollView>
  );
}
