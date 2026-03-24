import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  outerContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  headerBlock: {
    paddingHorizontal: 16,
    paddingTop: 12,
    gap: 12,
    backgroundColor: '#FFFFFF',
  },
  container: {
    flexGrow: 1,
    paddingHorizontal: 16,
    paddingTop: 20,
    paddingBottom: 16,
    gap: 24,
  },
  heroSection: {
    gap: 6,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    lineHeight: 32,
    color: '#111827',
  },
  description: {
    fontSize: 14,
    lineHeight: 21,
    color: '#6B7280',
  },
  bottomBar: {
    paddingHorizontal: 16,
    paddingTop: 8,
    backgroundColor: '#FFFFFF',
  },
});
