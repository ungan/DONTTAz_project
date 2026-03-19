import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    paddingHorizontal: 16,
    paddingTop: 32,
    paddingBottom: 24,
    backgroundColor: '#FFFFFF',
    justifyContent: 'space-between',
  },
  topSection: {
    gap: 24,
  },
  titleGroup: {
    gap: 8,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#111827',
  },
  description: {
    fontSize: 15,
    lineHeight: 22,
    color: '#6B7280',
  },
  pinSection: {
    alignItems: 'center',
    gap: 16,
    marginTop: 12,
  },
  pinLabel: {
    fontSize: 16,
    fontWeight: '600',
    color: '#374151',
  },
  errorText: {
    fontSize: 14,
    color: '#DC2626',
    textAlign: 'center',
  },
  helperText: {
    fontSize: 14,
    lineHeight: 20,
    color: '#6B7280',
    textAlign: 'center',
  },
  bottomSection: {
    gap: 12,
  },
});
