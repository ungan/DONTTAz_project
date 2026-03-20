import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { CustomInput, SelectInput } from '@/shared/ui';
import { styles } from './account-open-profile-form.styles';

const EMAIL_DOMAIN_OPTIONS = ['gmail.com', 'naver.com', 'kakao.com', '직접입력'];

interface AccountOpenProfileFormProps {
  customerName: string;
  englishName: string;
  phoneNumber: string;
  zipCode: string;
  address: string;
  addressDetail: string;
  emailLocalPart: string;
  emailDomain: string;
  customerNameError: string | null;
  englishNameError: string | null;
  phoneNumberError: string | null;
  addressError: string | null;
  emailError: string | null;
  isSmsAgreed: boolean;
  isAlimtalkAgreed: boolean;
  isMailAgreed: boolean;
  onCustomerNameChange: (value: string) => void;
  onEnglishNameChange: (value: string) => void;
  onPhoneNumberChange: (value: string) => void;
  onZipCodeChange: (value: string) => void;
  onAddressChange: (value: string) => void;
  onAddressDetailChange: (value: string) => void;
  onEmailLocalPartChange: (value: string) => void;
  onEmailDomainChange: (value: string) => void;
  onToggleSmsAgreed: () => void;
  onToggleAlimtalkAgreed: () => void;
  onToggleMailAgreed: () => void;
}

function ConsentRow({
  label,
  checked,
  onPress,
}: {
  label: string;
  checked: boolean;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity style={styles.consentRow} activeOpacity={0.8} onPress={onPress}>
      <View style={[styles.checkCircle, checked && styles.checkCircleActive]}>
        <Ionicons name={checked ? 'checkmark' : 'ellipse-outline'} size={16} color={checked ? '#FFFFFF' : '#9CA3AF'} />
      </View>
      <Text style={styles.consentLabel}>{label}</Text>
    </TouchableOpacity>
  );
}

export function AccountOpenProfileForm({
  customerName,
  englishName,
  phoneNumber,
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
  onCustomerNameChange,
  onEnglishNameChange,
  onPhoneNumberChange,
  onZipCodeChange,
  onAddressChange,
  onAddressDetailChange,
  onEmailLocalPartChange,
  onEmailDomainChange,
  onToggleSmsAgreed,
  onToggleAlimtalkAgreed,
  onToggleMailAgreed,
}: AccountOpenProfileFormProps) {
  const selectedDomain = EMAIL_DOMAIN_OPTIONS.includes(emailDomain) ? emailDomain : '직접입력';
  const isCustomDomain = selectedDomain === '직접입력';

  const handleDomainSelect = (value: string) => {
    if (value === '직접입력') {
      onEmailDomainChange('');
      return;
    }

    onEmailDomainChange(value);
  };

  return (
    <View style={styles.container}>
      <View style={styles.lockedFieldNotice}>
        <Text style={styles.lockedFieldNoticeText}>
          이름과 휴대전화번호는 본인인증 결과를 기준으로 자동 입력됩니다.
        </Text>
      </View>

      <CustomInput
        label="이름"
        value={customerName}
        onChangeText={onCustomerNameChange}
        placeholder="본인인증 결과가 표시됩니다"
        editable={false}
        style={styles.readonlyInput}
        errorMessage={customerNameError ?? undefined}
      />

      <CustomInput
        label="영문명"
        value={englishName}
        onChangeText={onEnglishNameChange}
        placeholder="영문명(대문자)"
        autoCapitalize="characters"
        errorMessage={englishNameError ?? undefined}
      />

      <CustomInput
        label="휴대전화번호"
        value={phoneNumber}
        onChangeText={onPhoneNumberChange}
        placeholder="본인인증 결과가 표시됩니다"
        keyboardType="number-pad"
        editable={false}
        style={styles.readonlyInput}
        errorMessage={phoneNumberError ?? undefined}
      />

      <View style={styles.consentGroup}>
        <ConsentRow label="SMS 수신" checked={isSmsAgreed} onPress={onToggleSmsAgreed} />
        <ConsentRow label="알림톡 수신" checked={isAlimtalkAgreed} onPress={onToggleAlimtalkAgreed} />
        <ConsentRow label="우편물 수신" checked={isMailAgreed} onPress={onToggleMailAgreed} />
      </View>

      <View style={styles.addressSection}>
        <Text style={styles.sectionLabel}>주소</Text>
        <View style={styles.addressRow}>
          <View style={styles.zipCodeField}>
            <CustomInput
              value={zipCode}
              onChangeText={onZipCodeChange}
              placeholder="우편번호"
              keyboardType="number-pad"
              errorMessage={undefined}
            />
          </View>
          <View style={styles.addressAction}>
            <TouchableOpacity style={styles.addressButton} activeOpacity={0.8}>
              <Text style={styles.addressButtonLabel}>주소찾기</Text>
            </TouchableOpacity>
          </View>
        </View>
        <CustomInput
          value={address}
          onChangeText={onAddressChange}
          placeholder="기본 주소"
          errorMessage={undefined}
        />
        <CustomInput
          value={addressDetail}
          onChangeText={onAddressDetailChange}
          placeholder="상세 주소"
          errorMessage={addressError ?? undefined}
        />
      </View>

      <View style={styles.emailSection}>
        <Text style={styles.sectionLabel}>이메일</Text>
        <View style={styles.emailRow}>
          <View style={styles.emailLocalField}>
            <CustomInput
              value={emailLocalPart}
              onChangeText={onEmailLocalPartChange}
              placeholder="아이디"
              errorMessage={undefined}
            />
          </View>
          <Text style={styles.emailAt}>@</Text>
          <View style={styles.emailDomainField}>
            <SelectInput
              value={selectedDomain}
              options={EMAIL_DOMAIN_OPTIONS}
              placeholder="도메인 선택"
              onChange={handleDomainSelect}
            />
          </View>
        </View>
        {isCustomDomain ? (
          <CustomInput
            value={emailDomain}
            onChangeText={onEmailDomainChange}
            placeholder="직접 입력"
            errorMessage={emailError ?? undefined}
          />
        ) : emailError ? (
          <Text style={styles.errorText}>{emailError}</Text>
        ) : null}
      </View>
    </View>
  );
}
