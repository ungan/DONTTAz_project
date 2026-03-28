import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    marginHorizontal: 20,
    marginTop: 8,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    paddingVertical: 8,
    marginBottom: 8,
  },
  item: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    paddingHorizontal: 16,
    paddingVertical: 12,
    gap: 12,
  },
  itemBorder: {
    borderBottomWidth: 1,
    borderBottomColor: '#F3F4F6',
  },
  info: {
    flex: 1,
  },
  topRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 12,
  },
  category: {
    fontSize: 14,
    fontWeight: '700',
    color: '#111827',
  },
  amount: {
    fontSize: 14,
    fontWeight: '700',
    color: '#111827',
  },
  categoryRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  meta: {
    fontSize: 13,
    color: '#374151',
    marginTop: 3,
  },
  badge: {
    borderWidth: 1,
    borderRadius: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
  },
  badgeOk: {
    borderColor: '#D1D5DB',
  },
  badgeOver: {
    borderColor: '#D1D5DB',
  },
  badgeUnplanned: {
    borderColor: '#D1D5DB',
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '500',
  },
  badgeTextOk: {
    color: '#9CA3AF',
  },
  badgeTextOver: {
    color: '#9CA3AF',
  },
  badgeTextUnplanned: {
    color: '#9CA3AF',
  },
});
