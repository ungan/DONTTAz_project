import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  card: {
    width: 350,
    borderRadius: 16,
    backgroundColor: '#FFF',
    paddingVertical: 16,
    paddingHorizontal: 24,
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: 20,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    alignSelf: 'stretch',
  },
  title: {
    color: '#101828',
    fontFamily: 'Roboto',
    fontSize: 16,
    fontWeight: '600',
    lineHeight: 24,
  },
  viewAll: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 2,
  },
  viewAllText: {
    color: '#101828',
    fontFamily: 'Roboto',
    fontSize: 14,
    fontWeight: '400',
    lineHeight: 20,
  },
  list: {
    alignSelf: 'stretch',
    flexDirection: 'column',
    gap: 12,
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  iconWrapper: {
    position: 'relative',
  },
  iconBox: {
    width: 36,
    height: 36,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
  },
  newDot: {
    position: 'absolute',
    top: -2,
    right: -2,
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: '#EF4444',
  },
  merchantName: {
    flex: 1,
    color: '#101828',
    fontFamily: 'Roboto',
    fontSize: 14,
    fontWeight: '500',
  },
  amount: {
    color: '#101828',
    fontFamily: 'Roboto',
    fontSize: 14,
    fontWeight: '700',
  },
  emptyText: {
    color: '#9CA3AF',
    fontFamily: 'Roboto',
    fontSize: 14,
    alignSelf: 'center',
    marginTop: 20,
  },
});
