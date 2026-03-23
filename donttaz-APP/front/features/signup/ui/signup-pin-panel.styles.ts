import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'space-between',
    paddingBottom: 24,
  },
  section: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    gap: 20,
    paddingHorizontal: 24,
  },
  title: {
    fontSize: 22,
    fontWeight: '700',
    color: '#111827',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: 14,
    lineHeight: 22,
    color: '#6B7280',
    textAlign: 'center',
  },
  indicatorBlock: {
    alignItems: 'center',
  },
  errorText: {
    fontSize: 13,
    lineHeight: 20,
    color: '#DC2626',
    textAlign: 'center',
    paddingHorizontal: 24,
  },
});
