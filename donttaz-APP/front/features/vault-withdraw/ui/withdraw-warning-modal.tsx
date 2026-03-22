import React from 'react';
import { Modal, View, Text, TouchableOpacity } from 'react-native';
import { styles } from './withdraw-warning-modal.styles';

interface WithdrawWarningModalProps {
  visible: boolean;
  penaltyRate: number;
  baseInterestRate: number;
  onCancel: () => void;
  onConfirm: () => void;
}

export function WithdrawWarningModal({
  visible,
  penaltyRate,
  baseInterestRate,
  onCancel,
  onConfirm,
}: WithdrawWarningModalProps) {
  const afterRate = Math.max(baseInterestRate - penaltyRate, 0).toFixed(1);

  return (
    <Modal visible={visible} transparent animationType="fade">
      <TouchableOpacity style={styles.overlay} activeOpacity={1} onPress={onCancel}>
        <TouchableOpacity style={styles.modal} activeOpacity={1}>

          <Text style={styles.title}>잠깐, 이 숫자를 보세요</Text>

          {/* 이율 비교 */}
          <View style={styles.rateRow}>
            <View style={styles.rateBox}>
              <Text style={styles.rateLabel}>현재 이율</Text>
              <Text style={styles.rateValue}>{baseInterestRate.toFixed(1)}%</Text>
            </View>
            <Text style={styles.arrow}>→</Text>
            <View style={styles.rateBox}>
              <Text style={styles.rateLabel}>출금 후</Text>
              <Text style={[styles.rateValue, styles.rateValueAfter]}>{afterRate}%</Text>
            </View>
          </View>

          <Text style={styles.penaltyBadge}>-{penaltyRate}%p 손해</Text>

          <Text style={styles.bonusHint}>
            참으면 최대 <Text style={styles.bonusHighlight}>연 6.0%</Text>까지 올라가요
          </Text>

          <View style={styles.buttonArea}>
            <TouchableOpacity style={styles.cancelButton} onPress={onCancel} activeOpacity={0.85}>
              <Text style={styles.cancelLabel}>😊  휴, 참는다</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.confirmButton} onPress={onConfirm} activeOpacity={0.7}>
              <Text style={styles.confirmLabel}>그래도 출금하기 →</Text>
            </TouchableOpacity>
          </View>

        </TouchableOpacity>
      </TouchableOpacity>
    </Modal>
  );
}
