import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    width: '100%',
    gap: 16,
  },
  verifiedCard: {
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    backgroundColor: '#F9FAFB',
    overflow: 'hidden',
  },
  verifiedCardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  verifiedCardBadge: {
    fontSize: 12,
    fontWeight: '600',
    color: Primary,
  },
  verifiedDivider: {
    height: 1,
    backgroundColor: '#E5E7EB',
  },
  verifiedRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  verifiedInnerDivider: {
    height: 1,
    backgroundColor: '#F3F4F6',
    marginHorizontal: 16,
  },
  verifiedLabel: {
    fontSize: 14,
    color: '#6B7280',
  },
  verifiedValue: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
  },
  consentGroup: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  consentRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  checkCircle: {
    width: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#D1D5DB',
    backgroundColor: '#FFFFFF',
  },
  checkCircleActive: {
    borderColor: Primary,
    backgroundColor: Primary,
  },
  consentLabel: {
    fontSize: 14,
    color: '#4B5563',
  },
  sectionLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  addressSection: {
    gap: 12,
  },
  addressRow: {
    flexDirection: 'row',
    gap: 12,
  },
  zipCodeField: {
    flex: 1,
  },
  addressAction: {
    width: 96,
    justifyContent: 'flex-end',
  },
  addressButton: {
    height: 52,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFFFFF',
  },
  addressButtonLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
  },
  emailSection: {
    gap: 12,
  },
  emailRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  emailLocalField: {
    flex: 1,
  },
  emailAt: {
    fontSize: 16,
    fontWeight: '600',
    color: '#374151',
  },
  emailDomainField: {
    flex: 1,
  },
  errorText: {
    fontSize: 12,
    color: '#EF4444',
  },
});
