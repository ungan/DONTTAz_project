import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    backgroundColor: '#F9F9F9',
    width: '100%',
    paddingHorizontal: 24,
    paddingTop: 24,
    paddingBottom: 24,
  },
  labelRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  labelLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  wishlistLink: {
    fontSize: 14,
    fontWeight: '500',
    color: '#6B7280',
  },
  amount: {
    fontSize: 32,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 16,
  },
  imageContainer: {
    alignItems: 'center',
    marginBottom: 20,
  },
  vaultImage: {
    width: 219,
    height: 132,
  },
  buttonRow: {
    flexDirection: 'row',
    gap: 10,
    justifyContent: 'center',
  },
  button: {
    paddingVertical: 9,
    paddingHorizontal: 24,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#D1D5DB',
    backgroundColor: '#FFFFFF',
    alignItems: 'center',
  },
  buttonPrimary: {},
  buttonOutline: {},
  buttonText: {
    fontSize: 14,
    fontWeight: '500',
  },
  buttonTextPrimary: {
    color: '#111827',
  },
  buttonTextOutline: {
    color: '#111827',
  },
});
