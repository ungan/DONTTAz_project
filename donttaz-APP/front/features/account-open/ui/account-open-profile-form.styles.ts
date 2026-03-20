import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    width: '100%',
    gap: 16,
  },
  lockedFieldNotice: {
    padding: 12,
    borderRadius: 12,
    backgroundColor: '#EFF6FF',
    borderWidth: 1,
    borderColor: '#BFDBFE',
  },
  lockedFieldNoticeText: {
    fontSize: 13,
    lineHeight: 20,
    color: '#1D4ED8',
  },
  readonlyInput: {
    color: '#6B7280',
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
