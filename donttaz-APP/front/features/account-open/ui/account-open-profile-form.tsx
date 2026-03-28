import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { CustomInput } from '@/shared/ui';
import { styles } from './account-open-profile-form.styles';

interface AccountOpenProfileFormProps {
  customerName: string;
  phoneNumber: string;
  zipCode: string;
  addressDetail: string;
  customerNameError: string | null;
  phoneNumberError: string | null;
  addressError: string | null;
  isSmsAgreed: boolean;
  isAlimtalkAgreed: boolean;
  isMailAgreed: boolean;
  onCustomerNameChange: (value: string) => void;
  onPhoneNumberChange: (value: string) => void;
  onZipCodeChange: (value: string) => void;
  onAddressDetailChange: (value: string) => void;
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
  phoneNumber,
  zipCode,
  addressDetail,
  customerNameError,
  phoneNumberError,
  addressError,
  isSmsAgreed,
  isAlimtalkAgreed,
  isMailAgreed,
  onCustomerNameChange,
  onPhoneNumberChange,
  onZipCodeChange,
  onAddressDetailChange,
  onToggleSmsAgreed,
  onToggleAlimtalkAgreed,
  onToggleMailAgreed,
}: AccountOpenProfileFormProps) {
  return (
    <View style={styles.container}>
      <CustomInput
        label="이름"
        value={customerName}
        onChangeText={onCustomerNameChange}
        placeholder="본인인증 결과가 표시됩니다"
        editable={false}
        errorMessage={customerNameError ?? undefined}
      />

      <CustomInput
        label="휴대전화번호"
        value={phoneNumber}
        onChangeText={onPhoneNumberChange}
        placeholder="본인인증 결과가 표시됩니다"
        keyboardType="number-pad"
        editable={false}
        errorMessage={phoneNumberError ?? undefined}
      />

      <View style={styles.consentGroup}>
        <ConsentRow label="SMS 수신" checked={isSmsAgreed} onPress={onToggleSmsAgreed} />
        <ConsentRow label="알림톡 수신" checked={isAlimtalkAgreed} onPress={onToggleAlimtalkAgreed} />
        <ConsentRow label="우편물 수신" checked={isMailAgreed} onPress={onToggleMailAgreed} />
      </View>

      <View style={styles.addressSection}>
        <Text style={styles.sectionLabel}>주소</Text>
        <CustomInput
          value={zipCode}
          onChangeText={onZipCodeChange}
          placeholder="우편번호"
          keyboardType="number-pad"
          errorMessage={undefined}
        />
        <CustomInput
          value={addressDetail}
          onChangeText={onAddressDetailChange}
          placeholder="상세 주소"
          errorMessage={addressError ?? undefined}
        />
      </View>
    </View>
  );
}
