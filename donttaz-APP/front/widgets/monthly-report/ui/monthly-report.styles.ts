import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  errorText: {
    fontSize: 14,
    color: '#EF4444',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 12,
  },
  totalLabel: {
    fontSize: 13,
    color: '#6B7280',
    marginBottom: 4,
  },
  totalAmount: {
    fontSize: 26,
    fontWeight: '700',
    color: '#111827',
  },
  monthBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
  monthText: {
    fontSize: 14,
    color: '#374151',
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    paddingHorizontal: 20,
    marginTop: 24,
    marginBottom: 12,
  },
});
