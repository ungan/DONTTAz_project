import React, { forwardRef, useCallback } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import BottomSheet, { BottomSheetView } from '@gorhom/bottom-sheet';
import { NumericKeypad, PinIndicator, ConfirmModal } from '@/shared/ui';
import { Primary } from '@/shared/constants/theme';
import { usePinInput } from '../model/use-pin-input';
import { useTransferSubmit } from '../model/use-transfer-submit';
import { styles } from './transfer-pin-sheet.styles';

export const TransferPinSheet = forwardRef<BottomSheet>((_, ref) => {
  const { submit, isLoading, errorMessage, clearError } = useTransferSubmit();

  const handleComplete = useCallback((pin: string) => {
    submit(pin);
  }, []);

  const { filledCount, handleKeyPress, reset } = usePinInput({ onComplete: handleComplete });

  return (
    <>
      <BottomSheet
        ref={ref}
        index={-1}
        enableDynamicSizing
        enablePanDownToClose={!isLoading}
        onClose={reset}
        backgroundStyle={{ backgroundColor: Primary }}
        handleIndicatorStyle={{ backgroundColor: 'rgba(255,255,255,0.6)' }}
      >
        <BottomSheetView style={styles.container}>
          {isLoading ? (
            <View style={styles.loadingArea}>
              <ActivityIndicator size="large" color="#FFFFFF" />
              <Text style={styles.loadingText}>송금 중...</Text>
            </View>
          ) : (
            <>
              <Text style={styles.title}>PIN 번호 6자리를 입력해주세요</Text>

              <View style={styles.pinArea}>
                <PinIndicator
                  filledCount={filledCount}
                  filledColor="#FFFFFF"
                  emptyBorderColor="rgba(255,255,255,0.5)"
                />
              </View>

              <NumericKeypad onKeyPress={handleKeyPress} hiddenKeys={['00']} labelColor="#FFFFFF" />
            </>
          )}
        </BottomSheetView>
      </BottomSheet>

      <ConfirmModal
        visible={!!errorMessage}
        title="송금 실패"
        message={errorMessage ?? ''}
        confirmLabel="확인"
        cancelLabel=""
        onConfirm={clearError}
        onCancel={clearError}
      />
    </>
  );
});
