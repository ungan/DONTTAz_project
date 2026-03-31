import React, { forwardRef } from 'react';
import { View, Text } from 'react-native';
import BottomSheet from '@gorhom/bottom-sheet';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { BottomSheetBase, CustomInput, Button } from '@/shared/ui';
import { useWishlistForm } from '../model/use-wishlist-form';
import { styles } from './wishlist-bottom-sheet.styles';

interface WishlistBottomSheetProps {
  onSubmit: (wishName: string, goalAmount: number) => Promise<void>;
  onClose?: () => void;
}

export const WishlistBottomSheet = forwardRef<BottomSheet, WishlistBottomSheetProps>(
  ({ onSubmit, onClose }, ref) => {
    const { bottom } = useSafeAreaInsets();
    const { wishName, setWishName, goalAmount, setGoalAmount, isValid } = useWishlistForm();

    const handleSubmit = async () => {
      console.log('[bottomsheet] handleSubmit called', wishName, goalAmount);
      console.log('[bottomsheet] onSubmit type:', typeof onSubmit);
      try {
        await onSubmit(wishName.trim(), Number(goalAmount));
        console.log('[bottomsheet] onSubmit done');
      } catch (e) {
        console.error('[bottomsheet] onSubmit error:', e);
      }
    };

    return (
      <BottomSheetBase ref={ref} onClose={onClose}>
        <View style={[styles.container, { paddingBottom: Math.max(bottom, 24) }]}>
          <Text style={styles.title}>위시리스트 추가</Text>

          <Text style={styles.label}>무엇을 원하시나요?</Text>
          <CustomInput
            value={wishName}
            onChangeText={setWishName}
            placeholder="예) 갤럭시 S25, 제주도 여행..."
            returnKeyType="next"
          />

          <Text style={styles.label}>목표 금액</Text>
          <View style={styles.amountRow}>
            <View style={styles.amountInputWrapper}>
              <CustomInput
                value={goalAmount}
                onChangeText={(v) => setGoalAmount(v.replace(/[^0-9]/g, ''))}
                placeholder="1,299,000"
                keyboardType="number-pad"
                returnKeyType="done"
              />
            </View>
            <Text style={styles.unit}>원</Text>
          </View>

          <Text style={styles.hint}>• 목표가 명확할수록 돈을 지켜내기가 훨씬 쉬워져요.</Text>

          <Button
            label="각서 작성하러 가기"
            disabled={!isValid}
            onPress={handleSubmit}
            style={styles.submitButton}
          />
        </View>
      </BottomSheetBase>
    );
  }
);
