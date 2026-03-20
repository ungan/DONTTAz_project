import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  keyboardAvoid: {
    flex: 1,
  },
  content: {
    paddingHorizontal: 16,
    paddingTop: 24,
    paddingBottom: 32,
    gap: 24,
  },
  question: {
    fontSize: 24,
    fontWeight: '700',
    color: '#111827',
    lineHeight: 32,
  },
  selectedAccountBox: {
    borderRadius: 16,
    backgroundColor: '#F9FAFB',
    padding: 16,
    gap: 6,
  },
  selectedAccountLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#6B7280',
  },
  selectedAccountValue: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  selectedAccountSubValue: {
    fontSize: 14,
    color: '#6B7280',
  },
  formAndButton: {
    gap: 20,
  },
});
