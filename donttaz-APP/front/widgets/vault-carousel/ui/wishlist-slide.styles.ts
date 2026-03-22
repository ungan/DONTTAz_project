import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    backgroundColor: '#F9F9F9',
    width: '100%',
    height: 339,
    paddingHorizontal: 24,
    paddingTop: 24,
  },
  labelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 16,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },

  // 없을 때
  emptyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingBottom: 24,
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
    textAlign: 'center',
  },
  emptyDesc: {
    fontSize: 13,
    color: '#6B7280',
    textAlign: 'center',
    lineHeight: 20,
  },
  registerButton: {
    marginTop: 8,
    paddingVertical: 10,
    paddingHorizontal: 28,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: '#D1D5DB',
  },
  registerButtonText: {
    fontSize: 14,
    fontWeight: '500',
    color: '#111827',
  },

  // 있을 때
  wishlistContent: {
    flex: 1,
  },
  editIconButton: {
    alignSelf: 'flex-end',
    padding: 4,
  },
  wishName: {
    fontSize: 24,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 4,
    lineHeight: 32,
  },
  amountText: {
    fontSize: 15,
    color: '#9CA3AF',
    marginBottom: 32,
  },

  // 프로그레스
  progressWrapper: {
    gap: 10,
  },
  progressTrack: {
    height: 26,
    borderRadius: 999,
    backgroundColor: '#E5E7EB',
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    borderRadius: 999,
    backgroundColor: '#FBB800',
  },
  progressLabels: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  progressPercent: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FBB800',
  },
  progressMax: {
    fontSize: 14,
    color: '#D1D5DB',
  },
});
