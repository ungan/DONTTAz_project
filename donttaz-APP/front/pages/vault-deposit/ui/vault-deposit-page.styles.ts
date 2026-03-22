import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },

  // 상단 취소 버튼 (우측 정렬)
  header: {
    alignItems: 'flex-end',
    paddingHorizontal: 20,
    paddingTop: 12,
    paddingBottom: 4,
  },
  cancelButton: {
    padding: 8,
  },
  cancelText: {
    fontSize: 16,
    color: '#6B7280',
  },

  // 금액 입력 영역 - flex:1 로 키패드 위 공간 차지
  amountArea: {
    flex: 1,
    paddingHorizontal: 28,
    paddingTop: 36,
    gap: 8,
  },

  // 현재 잔액 (흐리게)
  currentBalanceLabel: {
    fontSize: 13,
    color: '#C4C4C4',
    marginBottom: 2,
  },
  currentBalanceAmount: {
    fontSize: 30,
    fontWeight: '600',
    color: '#C4C4C4',
    marginBottom: 20,
  },

  // 입력 금액 행
  inputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  plus: {
    fontSize: 36,
    fontWeight: '400',
    color: '#111827',
    lineHeight: 46,
  },
  inputAmount: {
    fontSize: 42,
    fontWeight: '700',
    color: '#111827',
    lineHeight: 52,
  },
  inputAmountEmpty: {
    color: '#D1D5DB',
  },
  dimmed: {
    color: '#D1D5DB',
  },

  // 원 단위 지우기 버튼 (동그라미 ×)
  clearButton: {
    width: 26,
    height: 26,
    borderRadius: 13,
    backgroundColor: '#D1D5DB',
    alignItems: 'center',
    justifyContent: 'center',
  },
  clearIcon: {
    fontSize: 16,
    color: '#FFFFFF',
    lineHeight: 20,
  },

  inputUnit: {
    fontSize: 26,
    fontWeight: '400',
    color: '#111827',
    lineHeight: 46,
  },

  // 하단 확인 버튼 + 키패드
  bottomArea: {
    gap: 0,
  },
  confirmButton: {
    height: 62,
    backgroundColor: Primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  confirmButtonDisabled: {
    backgroundColor: '#93C5FD',
  },
  confirmText: {
    fontSize: 18,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  confirmTextDisabled: {
    color: '#FFFFFF',
  },
});
