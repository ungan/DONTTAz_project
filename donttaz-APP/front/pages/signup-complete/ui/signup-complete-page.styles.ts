import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  outerContainer: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  container: {
    flexGrow: 1,
    paddingTop: 20,
    paddingBottom: 16,
  },
  headerBlock: {
    marginHorizontal: 0,
  },
  heroSection: {
    alignItems: 'center',
    paddingTop: 72,
    paddingBottom: 40,
    gap: 20,
    paddingHorizontal: 24,
  },
  heroTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
    textAlign: 'center',
  },
  summaryList: {
    borderTopWidth: 1.5,
    borderBottomWidth: 1.5,
    borderColor: '#111827',
    marginTop: 48,
    marginHorizontal: 24,
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 18,
  },
  divider: {
    height: 1,
    backgroundColor: '#E5E7EB',
  },
  summaryLabel: {
    fontSize: 14,
    color: '#9CA3AF',
  },
  summaryValue: {
    fontSize: 14,
    fontWeight: '500',
    color: '#111827',
  },
  bottomBar: {
    paddingHorizontal: 16,
    paddingTop: 8,
    gap: 8,
    backgroundColor: '#FFFFFF',
  },
  errorText: {
    fontSize: 14,
    lineHeight: 22,
    color: '#DC2626',
  },
});
