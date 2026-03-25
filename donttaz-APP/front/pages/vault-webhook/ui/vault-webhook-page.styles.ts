import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#F9FAFB',
  },
  scrollContent: {
    flexGrow: 1,
    paddingVertical: 24,
    paddingBottom: 40,
    gap: 20,
  },

  heroSection: {
    paddingHorizontal: 20,
    paddingBottom: 8,
    gap: 8,
  },
  heroTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#111827',
    lineHeight: 32,
  },
  heroText: {
    fontSize: 15,
    color: '#6B7280',
    lineHeight: 22,
  },

  card: {
    marginHorizontal: 16,
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    gap: 14,
    borderWidth: 1,
    borderColor: '#E5E7EB',
  },
  cardLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: '#6B7280',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },

  // 플랫폼
  platformGrid: {
    flexDirection: 'row',
    gap: 12,
  },
  platformCard: {
    flex: 1,
    flexBasis: 0,
    height: 96,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#E5E7EB',
    backgroundColor: '#FAFAFA',
    paddingHorizontal: 14,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  platformCardSelected: {
    borderColor: '#3B82F6',
    backgroundColor: '#F0F6FF',
  },
  platformIconBox: {
    width: 44,
    height: 44,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  platformLabel: {
    fontSize: 13,
    fontWeight: '500',
    color: '#6B7280',
  },
  platformLabelSelected: {
    color: '#111827',
    fontWeight: '600',
  },

  // 메세지
  messageInputWrapper: {
    borderWidth: 1,
    borderColor: '#E5E7EB',
    borderRadius: 10,
    backgroundColor: '#F9FAFB',
    paddingHorizontal: 14,
    paddingTop: 12,
    paddingBottom: 8,
  },
  messageInput: {
    fontSize: 14,
    color: '#111827',
    lineHeight: 22,
    minHeight: 110,
    textAlignVertical: 'top',
  },
  messageCounter: {
    fontSize: 12,
    color: '#9CA3AF',
    textAlign: 'right',
    marginTop: 4,
  },
  messageCounterOver: {
    color: '#EF4444',
  },

  infoBox: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  infoText: {
    fontSize: 12,
    color: '#9CA3AF',
  },

  buttonWrapper: {
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderTopWidth: 1,
    borderTopColor: '#F3F4F6',
    backgroundColor: '#F9FAFB',
  },
});
