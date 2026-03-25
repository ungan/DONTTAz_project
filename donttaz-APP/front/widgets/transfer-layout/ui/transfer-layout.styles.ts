import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  recipientRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
    paddingBottom: 4,
    gap: 6,
  },
  recipientBank: {
    fontSize: 15,
    fontWeight: '600',
    color: '#374151',
  },
  recipientAccount: {
    fontSize: 15,
    color: '#9CA3AF',
  },
  amountArea: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  amountRow: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 4,
  },
  amountText: {
    fontSize: 44,
    fontWeight: '700',
    color: '#111827',
    letterSpacing: -1,
  },
  amountPlaceholder: {
    fontSize: 44,
    fontWeight: '700',
    color: '#D1D5DB',
    letterSpacing: -1,
  },
  amountUnit: {
    fontSize: 22,
    fontWeight: '500',
    color: '#111827',
    marginBottom: 4,
  },
  amountUnitPlaceholder: {
    fontSize: 22,
    fontWeight: '500',
    color: '#D1D5DB',
    marginBottom: 4,
  },
});
