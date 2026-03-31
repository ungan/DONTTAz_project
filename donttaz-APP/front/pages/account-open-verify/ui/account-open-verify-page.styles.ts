import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingHorizontal: 16,
    paddingTop: 12,
    backgroundColor: '#FFFFFF',
    gap: 28,
  },
  headerBlock: {
    gap: 12,
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
  infoCard: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    backgroundColor: '#F9FAFB',
    overflow: 'hidden',
  },
  verifiedBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  verifiedBadgeText: {
    fontSize: 13,
    fontWeight: '600',
    color: '#5899FD',
  },
  divider: {
    height: 1,
    backgroundColor: '#E5E7EB',
  },
  innerDivider: {
    height: 1,
    backgroundColor: '#E5E7EB',
    marginHorizontal: 16,
  },
  innerDividerVerified: {
    backgroundColor: '#FFFFFF',
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  summaryLabel: {
    fontSize: 14,
    color: '#6B7280',
  },
  summaryValue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
  },
  errorText: {
    fontSize: 13,
    color: '#EF4444',
  },
  helperText: {
    fontSize: 13,
    lineHeight: 19,
    color: '#9CA3AF',
    textAlign: 'center',
  },
  bottomBar: {
    paddingHorizontal: 16,
    paddingTop: 8,
    gap: 10,
    backgroundColor: '#FFFFFF',
  },
});
