import React, { forwardRef, useState } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import BottomSheet from '@gorhom/bottom-sheet';
import { Calendar } from 'react-native-calendars';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { BottomSheetBase, CustomInput, Button } from '@/shared/ui';
import type { Wishlist, WishlistRequest } from '@/entities/vault';
import { useWishlistForm } from '../model/use-wishlist-form';
import { styles } from './wishlist-bottom-sheet.styles';

interface WishlistBottomSheetProps {
  editTarget?: Wishlist;
  onSubmit: (body: WishlistRequest, wishId?: number) => Promise<void>;
  onClose?: () => void;
}

export const WishlistBottomSheet = forwardRef<BottomSheet, WishlistBottomSheetProps>(
  ({ editTarget, onSubmit, onClose }, ref) => {
    const { bottom } = useSafeAreaInsets();
    const {
      wishName, setWishName,
      goalAmount, setGoalAmount,
      targetDate, setTargetDate,
      isValid,
      getRequestBody,
    } = useWishlistForm(editTarget);
    const [showDatePicker, setShowDatePicker] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async () => {
      setIsLoading(true);
      await onSubmit(getRequestBody(), editTarget?.id);
      setIsLoading(false);
      (ref as React.RefObject<BottomSheet>)?.current?.close();
    };

    const formattedDate = targetDate.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });

    return (
      <BottomSheetBase ref={ref} onClose={onClose}>
        <View style={[styles.container, { paddingBottom: Math.max(bottom, 24) }]}>
          <Text style={styles.title}>
            위시리스트 추가
          </Text>

          <Text style={styles.label}>무엇을 살 건가요?</Text>
          <CustomInput
            value={wishName}
            onChangeText={setWishName}
            placeholder="예) 갤럭시 S25, 제주도 여행..."
            returnKeyType="done"
          />

          <Text style={styles.label}>목표 금액</Text>
          <View style={styles.amountRow}>
            <CustomInput
              style={styles.amountInput}
              value={goalAmount}
              onChangeText={(v) => setGoalAmount(v.replace(/[^0-9]/g, ''))}
              placeholder="1,299,000"
              keyboardType="number-pad"
              returnKeyType="done"
            />
            <Text style={styles.unit}>원</Text>
          </View>

          <Text style={styles.label}>목표 날짜</Text>
          <TouchableOpacity style={styles.dateButton} onPress={() => setShowDatePicker(true)}>
            <Text style={styles.dateText}>{formattedDate}</Text>
          </TouchableOpacity>

          {showDatePicker && (
            <Calendar
              current={targetDate.toISOString().split('T')[0]}
              minDate={new Date().toISOString().split('T')[0]}
              onDayPress={(day) => {
                setTargetDate(new Date(day.dateString));
                setShowDatePicker(false);
              }}
              markedDates={{
                [targetDate.toISOString().split('T')[0]]: {
                  selected: true,
                  selectedColor: '#5899FD',
                },
              }}
              theme={{
                todayTextColor: '#5899FD',
                selectedDayBackgroundColor: '#5899FD',
                arrowColor: '#5899FD',
              }}
            />
          )}

          <Text style={styles.hint}>• 목표가 뚜렷할수록 돈을 안 쓰기가 훨씬 쉬워져요.</Text>

          <Button
            label={editTarget ? '수정하기' : '목표를 먼저 알려주세요'}
            disabled={!isValid}
            isLoading={isLoading}
            onPress={handleSubmit}
            style={styles.submitButton}
          />
        </View>
      </BottomSheetBase>
    );
  }
);
