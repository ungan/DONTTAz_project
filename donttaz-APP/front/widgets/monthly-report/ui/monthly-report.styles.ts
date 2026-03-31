import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  contentContainer: {
    paddingBottom: 28,
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  centerCardWrap: {
    flex: 1,
    justifyContent: 'center',
    paddingHorizontal: 24,
  },
  stateCard: {
    borderRadius: 18,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    paddingHorizontal: 24,
    paddingVertical: 24,
  },
  stateTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 10,
  },
  stateDescription: {
    fontSize: 14,
    lineHeight: 21,
    color: '#6B7280',
  },
  errorText: {
    fontSize: 14,
    color: '#EF4444',
  },
  monthNav: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 16,
    paddingTop: 16,
    paddingBottom: 4,
  },
  monthNavText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    minWidth: 100,
    textAlign: 'center',
  },
  header: {
    paddingHorizontal: 24,
    paddingTop: 12,
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
  summaryRow: {
    flexDirection: 'row',
    gap: 12,
    paddingHorizontal: 24,
    marginTop: 8,
  },
  summaryCard: {
    flex: 1,
    borderRadius: 16,
    backgroundColor: '#F8FAFC',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    paddingHorizontal: 16,
    paddingVertical: 14,
  },
  summaryCardFull: {
    flex: 0,
    flexBasis: '100%',
  },
  summaryCardLabel: {
    fontSize: 12,
    color: '#6B7280',
    marginBottom: 6,
  },
  summaryCardValue: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  feedbackCard: {
    marginHorizontal: 24,
    marginTop: 16,
    borderRadius: 18,
    backgroundColor: '#EEF5FF',
    paddingHorizontal: 18,
    paddingVertical: 18,
  },
  feedbackTitleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginBottom: 8,
  },
  feedbackTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#335C9E',
  },
  feedbackBody: {
    fontSize: 14,
    lineHeight: 21,
    color: '#1F2937',
  },
  emptyCard: {
    marginHorizontal: 24,
    marginTop: 20,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 24,
    paddingVertical: 22,
    alignItems: 'center',
  },
  emptyTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
    marginBottom: 8,
  },
  emptyDescription: {
    fontSize: 13,
    lineHeight: 20,
    color: '#6B7280',
    textAlign: 'center',
  },
  divider: {
    height: 1,
    backgroundColor: '#F3F4F6',
    marginHorizontal: 24,
    marginTop: 24,
    marginBottom: 0,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    paddingHorizontal: 24,
    marginTop: 24,
    marginBottom: 12,
  },
  personalizedFeedbackCard: {
    marginHorizontal: 24,
    marginBottom: 28,
    borderRadius: 16,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    paddingVertical: 8,
  },
  personalizedFeedbackItem: {
    paddingHorizontal: 16,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F3F4F6',
  },
  personalizedFeedbackItemLast: {
    borderBottomWidth: 0,
  },
  personalizedFeedbackHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 6,
  },
  feedbackBadge: {
    borderRadius: 999,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  feedbackBadgePersona: {
    backgroundColor: '#EDE9FE',
  },
  feedbackBadgeWarning: {
    backgroundColor: '#FEE2E2',
  },
  feedbackBadgePattern: {
    backgroundColor: '#DBEAFE',
  },
  feedbackBadgePositive: {
    backgroundColor: '#DCFCE7',
  },
  personalizedFeedbackType: {
    fontSize: 12,
    fontWeight: '700',
  },
  feedbackBadgeTextPersona: {
    color: '#6D28D9',
  },
  feedbackBadgeTextWarning: {
    color: '#B91C1C',
  },
  feedbackBadgeTextPattern: {
    color: '#1D4ED8',
  },
  feedbackBadgeTextPositive: {
    color: '#166534',
  },
  personalizedFeedbackCategory: {
    fontSize: 12,
    color: '#6B7280',
  },
  personalizedFeedbackMessage: {
    fontSize: 14,
    lineHeight: 20,
    color: '#111827',
  },
  feedbackMessageGap: {
    marginTop: 5,
  },
});
