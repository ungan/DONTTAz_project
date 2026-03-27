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
    paddingHorizontal: 20,
  },
  stateCard: {
    borderRadius: 18,
    backgroundColor: '#FFFFFF',
    borderWidth: 1,
    borderColor: '#E5E7EB',
    paddingHorizontal: 20,
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
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
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
  headerRight: {
    alignItems: 'flex-end',
    gap: 8,
  },
  selectorWrap: {
    paddingHorizontal: 20,
    marginTop: 4,
  },
  monthBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#D1D5DB',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 4,
    backgroundColor: '#FFFFFF',
  },
  monthText: {
    fontSize: 14,
    color: '#374151',
  },
  statusBadge: {
    borderRadius: 999,
    paddingHorizontal: 10,
    paddingVertical: 6,
  },
  statusBadgeDanger: {
    backgroundColor: '#FEE2E2',
  },
  statusBadgeSafe: {
    backgroundColor: '#DCFCE7',
  },
  statusText: {
    fontSize: 12,
    fontWeight: '700',
  },
  statusTextDanger: {
    color: '#B91C1C',
  },
  statusTextSafe: {
    color: '#166534',
  },
  summaryRow: {
    flexDirection: 'row',
    gap: 12,
    paddingHorizontal: 20,
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
    marginHorizontal: 20,
    marginTop: 16,
    borderRadius: 18,
    backgroundColor: '#EEF5FF',
    paddingHorizontal: 18,
    paddingVertical: 18,
  },
  feedbackTitle: {
    fontSize: 14,
    fontWeight: '700',
    color: '#335C9E',
    marginBottom: 8,
  },
  feedbackBody: {
    fontSize: 14,
    lineHeight: 21,
    color: '#1F2937',
  },
  emptyCard: {
    marginHorizontal: 20,
    marginTop: 20,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    backgroundColor: '#FFFFFF',
    paddingHorizontal: 20,
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
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
    paddingHorizontal: 20,
    marginTop: 24,
    marginBottom: 12,
  },
  personalizedFeedbackCard: {
    marginHorizontal: 20,
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
});
