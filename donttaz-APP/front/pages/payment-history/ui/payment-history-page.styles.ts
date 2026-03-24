import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F9FAFB',
  },
  // 상단 계좌 정보 — PageHeader와 같은 배경으로 자연스럽게 이어짐
  accountSection: {
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 24,
    paddingTop: 40,
    paddingBottom: 40,
    alignItems: 'center',
    gap: 10,
  },
  accountNo: {
    fontSize: 14,
    color: '#9CA3AF',
    letterSpacing: 0.8,
    textDecorationLine: 'underline',
    textDecorationColor: '#9CA3AF',
  },
  accountBalance: {
    fontSize: 40,
    fontWeight: '700',
    color: '#111827',
    letterSpacing: -1,
  },
  transferButton: {
    marginTop: 20,
    width: '100%',
    backgroundColor: '#EFF6FF',
    borderRadius: 12,
    paddingVertical: 14,
    alignItems: 'center',
  },
  transferButtonText: {
    fontSize: 15,
    fontWeight: '600',
    color: '#2563EB',
  },
  copyToast: {
    position: 'absolute',
    top: -28,
    alignSelf: 'center',
    backgroundColor: '#1F2937',
    borderRadius: 8,
    paddingHorizontal: 14,
    paddingVertical: 6,
  },
  copyToastText: {
    fontSize: 12,
    color: '#FFFFFF',
  },
  // 거래내역 리스트 영역
  listArea: {
    flex: 1,
    backgroundColor: '#F9FAFB',
  },
  list: {
    paddingHorizontal: 16,
    paddingTop: 12,
    paddingBottom: 32,
  },
  item: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFFFFF',
    borderRadius: 14,
    padding: 16,
    marginBottom: 8,
    gap: 12,
  },
  iconBox: {
    width: 42,
    height: 42,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  infoGroup: {
    flex: 1,
    gap: 3,
  },
  counterparty: {
    fontSize: 15,
    fontWeight: '600',
    color: '#111827',
  },
  itemDate: {
    fontSize: 12,
    color: '#9CA3AF',
  },
  amountGroup: {
    alignItems: 'flex-end',
    gap: 3,
  },
  amountWithdrawal: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },
  amountDeposit: {
    fontSize: 15,
    fontWeight: '700',
    color: '#2563EB',
  },
  balanceAfter: {
    fontSize: 12,
    color: '#9CA3AF',
  },
  footer: {
    paddingVertical: 24,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 13,
    color: '#9CA3AF',
  },
  emptyContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 80,
  },
  emptyText: {
    fontSize: 14,
    color: '#9CA3AF',
  },
  errorText: {
    fontSize: 14,
    color: '#DC2626',
    textAlign: 'center',
    paddingTop: 80,
  },
});
