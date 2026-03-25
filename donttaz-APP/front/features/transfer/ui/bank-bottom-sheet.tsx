import React, { forwardRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet } from 'react-native';
import BottomSheet from '@gorhom/bottom-sheet';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import { BottomSheetBase } from '@/shared/ui';
import { BANK_OPTIONS } from '../types/transfer';
import type { BankName } from '../types/transfer';

interface BankBottomSheetProps {
  selected: BankName | '';
  onSelect: (bank: BankName) => void;
  onClose?: () => void;
}

export const BankBottomSheet = forwardRef<BottomSheet, BankBottomSheetProps>(
  ({ selected, onSelect, onClose }, ref) => {
    const { bottom } = useSafeAreaInsets();

    const handleSelect = (bank: BankName) => {
      onSelect(bank);
      (ref as React.RefObject<BottomSheet>)?.current?.close();
    };

    return (
      <BottomSheetBase ref={ref} onClose={onClose}>
        <View style={[styles.container, { paddingBottom: Math.max(bottom, 24) }]}>
          <Text style={styles.title}>은행 선택</Text>
          <View style={styles.grid}>
            {BANK_OPTIONS.map((bank) => {
              const isSelected = selected === bank;
              return (
                <TouchableOpacity
                  key={bank}
                  style={[styles.cell, isSelected && styles.cellSelected]}
                  onPress={() => handleSelect(bank)}
                  activeOpacity={0.7}
                >
                  <Text style={[styles.cellText, isSelected && styles.cellTextSelected]}>
                    {bank}
                  </Text>
                  {isSelected && (
                    <Ionicons name="checkmark" size={14} color="#3B82F6" style={styles.check} />
                  )}
                </TouchableOpacity>
              );
            })}
          </View>
        </View>
      </BottomSheetBase>
    );
  }
);

const styles = StyleSheet.create({
  container: {
    paddingHorizontal: 20,
    paddingTop: 8,
  },
  title: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 16,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
  },
  cell: {
    width: '30.5%',
    height: 48,
    borderRadius: 12,
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#F3F4F6',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 4,
  },
  cellSelected: {
    backgroundColor: '#EFF6FF',
    borderColor: '#3B82F6',
  },
  cellText: {
    fontSize: 13,
    fontWeight: '500',
    color: '#374151',
  },
  cellTextSelected: {
    color: '#3B82F6',
    fontWeight: '700',
  },
  check: {
    marginLeft: 2,
  },
});
