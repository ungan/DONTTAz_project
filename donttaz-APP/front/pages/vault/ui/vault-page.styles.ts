import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  content: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyContainer: {
    flex: 1,
    paddingTop: 100,
    alignItems: 'center',
    justifyContent: 'flex-start',
    gap: 12,
  },
  emptyText: {
    fontSize: 15,
    color: '#9CA3AF',
  },
  emptyTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: '#111827',
  },
  emptyDesc: {
    fontSize: 14,
    color: '#6B7280',
    textAlign: 'center',
    lineHeight: 22,
  },
  registerButton: {
    marginTop: 8,
    paddingVertical: 12,
    paddingHorizontal: 32,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: '#D1D5DB',
  },
  registerButtonText: {
    fontSize: 15,
    fontWeight: '500',
    color: '#111827',
  },
});

export const wishlistStyles = StyleSheet.create({
  emptyIconWrapper: {
    width: 88,
    height: 88,
    borderRadius: 44,
    backgroundColor: '#F3F4F6',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  progressSection: {
    marginHorizontal: 20,
    marginBottom: 8,
    padding: 16,
    backgroundColor: '#F9FAFB',
    borderRadius: 12,
  },
  progressHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  wishName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
    flexShrink: 1,
    marginRight: 8,
  },
  progressPercent: {
    fontSize: 15,
    fontWeight: '700',
    color: '#4F46E5',
  },
  progressTrack: {
    height: 8,
    backgroundColor: '#E5E7EB',
    borderRadius: 4,
    overflow: 'hidden',
    marginBottom: 8,
  },
  progressFill: {
    height: '100%',
    backgroundColor: '#4F46E5',
    borderRadius: 4,
  },
  progressAmounts: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  progressCurrent: {
    fontSize: 13,
    color: '#4F46E5',
    fontWeight: '600',
  },
  progressGoal: {
    fontSize: 13,
    color: '#9CA3AF',
  },
});
