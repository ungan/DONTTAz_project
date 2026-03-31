import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  outerContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  container: {
    flexGrow: 1,
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 16,
    gap: 28,
  },
  headerBlock: {
    gap: 12,
  },
  heroSection: {
    gap: 8,
  },
  heroTitle: {
    fontSize: 24,
    fontWeight: '700',
    lineHeight: 34,
    color: '#111827',
  },
  heroDescription: {
    fontSize: 14,
    lineHeight: 21,
    color: '#6B7280',
  },
  termsGroup: {
    gap: 20,
  },
  bottomBar: {
    paddingHorizontal: 16,
    paddingTop: 8,
    gap: 8,
    backgroundColor: '#FFFFFF',
  },
  errorText: {
    fontSize: 13,
    color: '#EF4444',
    textAlign: 'center',
  },
});
