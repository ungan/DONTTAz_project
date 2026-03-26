import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  card: {
    borderRadius: 20,
    backgroundColor: '#FFFFFF',
    paddingVertical: 20,
    paddingHorizontal: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 10,
    elevation: 2,
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
  },
  headerDescription: {
    marginTop: 6,
    fontSize: 13,
    lineHeight: 19,
    color: '#6B7280',
  },
  chartWrap: {
    marginTop: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  chartCenter: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  centerLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: '#6B7280',
  },
  centerAmount: {
    marginTop: 4,
    fontSize: 20,
    fontWeight: '800',
    color: '#111827',
  },
  legend: {
    marginTop: 18,
  },
  legendRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 999,
  },
  legendInfo: {
    flex: 1,
    marginLeft: 10,
  },
  legendCategory: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
  },
  legendMeta: {
    marginTop: 2,
    fontSize: 12,
    color: '#6B7280',
  },
  legendAmount: {
    fontSize: 14,
    fontWeight: '700',
    color: '#111827',
  },
});
