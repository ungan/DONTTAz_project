import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 24,
    paddingBottom: 16,
  },
  topSection: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 20,
  },
  icon: {
    width: 96,
    height: 96,
  },
  textArea: {
    alignItems: 'center',
    gap: 8,
  },
  amountText: {
    fontSize: 36,
    fontWeight: '700',
    color: '#111827',
    letterSpacing: -1,
  },
  recipientText: {
    fontSize: 16,
    color: '#6B7280',
    textAlign: 'center',
    lineHeight: 24,
  },
  detailCard: {
    borderRadius: 16,
    backgroundColor: '#F9FAFB',
    borderWidth: 1,
    borderColor: '#F3F4F6',
    marginBottom: 20,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 16,
    paddingHorizontal: 20,
  },
  detailDivider: {
    height: 1,
    backgroundColor: '#F3F4F6',
    marginHorizontal: 20,
  },
  detailLabel: {
    fontSize: 14,
    color: '#9CA3AF',
  },
  detailValue: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  buttonArea: {
    gap: 4,
  },
  confirmButton: {
    height: 54,
    borderRadius: 14,
    backgroundColor: Primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  confirmButtonLabel: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  extraButton: {
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  extraButtonLabel: {
    fontSize: 14,
    color: '#9CA3AF',
  },
});
