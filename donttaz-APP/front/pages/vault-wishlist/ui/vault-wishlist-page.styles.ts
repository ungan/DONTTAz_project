import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  emptyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
    gap: 12,
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
  listContent: {
    paddingHorizontal: 24,
    paddingVertical: 24,
    gap: 16,
  },
  card: {
    backgroundColor: '#F9F9F9',
    borderRadius: 16,
    padding: 24,
    gap: 8,
  },
  wishName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#6B7280',
  },
  goalAmount: {
    fontSize: 28,
    fontWeight: '700',
    color: '#111827',
  },
  targetDate: {
    fontSize: 13,
    color: '#9CA3AF',
  },
  editButton: {
    marginTop: 12,
    alignSelf: 'flex-start',
    paddingVertical: 8,
    paddingHorizontal: 20,
    borderRadius: 999,
    borderWidth: 1,
    borderColor: '#D1D5DB',
  },
  editButtonText: {
    fontSize: 13,
    fontWeight: '500',
    color: '#6B7280',
  },
});
