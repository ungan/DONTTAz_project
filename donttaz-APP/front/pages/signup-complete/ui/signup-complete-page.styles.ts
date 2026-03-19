import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    paddingHorizontal: 16,
    paddingTop: 20,
    paddingBottom: 32,
    backgroundColor: '#FFFFFF',
    gap: 24,
  },
  section: {
    gap: 12,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: '#111827',
  },
  description: {
    fontSize: 15,
    lineHeight: 24,
    color: '#4B5563',
  },
  statusCard: {
    borderRadius: 16,
    padding: 20,
    backgroundColor: '#F8FAFC',
    gap: 12,
  },
  statusText: {
    fontSize: 15,
    lineHeight: 24,
    color: '#374151',
  },
  errorText: {
    fontSize: 14,
    lineHeight: 22,
    color: '#DC2626',
  },
  helperText: {
    fontSize: 13,
    lineHeight: 20,
    color: '#6B7280',
  },
});
