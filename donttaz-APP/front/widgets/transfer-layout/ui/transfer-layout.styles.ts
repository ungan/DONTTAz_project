import { StyleSheet, Dimensions } from 'react-native';

const { height } = Dimensions.get('window');

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  accountSummary: {
    fontSize: 14,
    color: '#9CA3AF',
    textAlign: 'center',
    paddingBottom: 8,
  },
  amountArea: {
    height: height * 0.3,
    alignItems: 'center',
    justifyContent: 'center',
  },
  amountText: {
    fontSize: 40,
    fontWeight: '500',
    color: '#111827',
  },
  amountPlaceholder: {
    fontSize: 40,
    fontWeight: '500',
    color: '#E0E4EB',
  },
  cancelLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
  },
});
