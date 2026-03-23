import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  pinSection: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    paddingHorizontal: 24,
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#111827',
  },
  subtitle: {
    fontSize: 16,
    color: '#6B7280',
  },
  indicatorWrapper: {
    marginTop: 8,
    marginBottom: 4,
  },
  errorText: {
    fontSize: 13,
    color: '#DC2626',
    textAlign: 'center',
  },
  helperText: {
    fontSize: 13,
    color: '#9CA3AF',
    textAlign: 'center',
  },
  keypadSection: {
    paddingBottom: 16,
  },
  switchAccountButton: {
    alignItems: 'center',
    paddingVertical: 16,
  },
  switchAccountText: {
    fontSize: 14,
    color: '#9CA3AF',
    textDecorationLine: 'underline',
  },
});
