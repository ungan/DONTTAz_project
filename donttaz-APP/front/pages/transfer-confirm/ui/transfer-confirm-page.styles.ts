import { StyleSheet } from 'react-native';
import { Primary } from '@/shared/constants/theme';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#FFFFFF',
    paddingTop: 200,
    paddingHorizontal: 16,
    paddingBottom: 60,
    alignItems: 'center',
  },
  image: {
    width: 88,
    height: 88,
  },
  amountText: {
    marginTop: 32,
    textAlign: 'center',
    fontSize: 30,
    fontWeight: '600',
    color: '#000',
    letterSpacing: 0.03,
    lineHeight: 42,
  },
  amountNameText: {
    fontSize: 30,
    fontWeight: '800',
    color: Primary,
    letterSpacing: 0.03,
  },
  accountSection: {
    marginTop: 48,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 16,
  },
  accountLabel: {
    fontSize: 18,
    fontWeight: '700',
    color: '#959595',
    letterSpacing: 0.018,
  },
  accountValue: {
    fontSize: 18,
    fontWeight: '700',
    color: '#959595',
    letterSpacing: 0.018,
  },
  buttonRow: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 16,
  },
  extraButton: {
    width: 150,
    height: 54,
    borderRadius: 15,
    backgroundColor: '#f7f7f7',
    alignItems: 'center',
    justifyContent: 'center',
  },
  extraButtonLabel: {
    fontSize: 20,
    fontWeight: '700',
    color: '#157FD7',
  },
  confirmButton: {
    width: 170,
    height: 54,
    borderRadius: 15,
    backgroundColor: Primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  confirmButtonLabel: {
    fontSize: 20,
    fontWeight: '700',
    color: '#FFFFFF',
  },
});
