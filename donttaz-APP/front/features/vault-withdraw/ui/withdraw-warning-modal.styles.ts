import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.45)',
    justifyContent: 'center',
    paddingHorizontal: 32,
  },
  modal: {
    backgroundColor: '#FFFFFF',
    borderRadius: 20,
    paddingVertical: 28,
    paddingHorizontal: 24,
    alignItems: 'center',
    gap: 16,
  },
  title: {
    fontSize: 17,
    fontWeight: '700',
    color: '#111827',
  },

  // 이율 비교
  rateRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
    marginTop: 4,
  },
  rateBox: {
    alignItems: 'center',
    gap: 4,
  },
  rateLabel: {
    fontSize: 12,
    color: '#9CA3AF',
  },
  rateValue: {
    fontSize: 32,
    fontWeight: '800',
    color: '#111827',
  },
  rateValueAfter: {
    color: '#EF4444',
  },
  arrow: {
    fontSize: 20,
    color: '#D1D5DB',
    marginTop: 16,
  },

  penaltyBadge: {
    fontSize: 13,
    fontWeight: '600',
    color: '#EF4444',
    backgroundColor: '#FEF2F2',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 999,
    overflow: 'hidden',
  },

  bonusHint: {
    fontSize: 13,
    color: '#9CA3AF',
    marginBottom: 4,
  },
  bonusHighlight: {
    fontWeight: '700',
    color: '#374151',
  },

  buttonArea: {
    width: '100%',
    gap: 8,
  },
  cancelButton: {
    height: 52,
    borderRadius: 12,
    backgroundColor: Primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelLabel: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  confirmButton: {
    alignItems: 'center',
    paddingVertical: 8,
  },
  confirmLabel: {
    fontSize: 14,
    color: '#9CA3AF',
  },
});
