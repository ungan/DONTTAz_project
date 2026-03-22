import { StyleSheet } from 'react-native';

export const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FFFFFF',
  },
  scrollContent: {
    flexGrow: 1,
    paddingVertical: 20,
    paddingBottom: 32,
    gap: 12,
  },

  // 상단 타이틀 (카드 밖)
  heroSection: {
    paddingHorizontal: 20,
    paddingBottom: 8,
    gap: 6,
  },
  heroTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: '#111827',
    lineHeight: 30,
  },
  heroText: {
    fontSize: 14,
    color: '#6B7280',
    lineHeight: 22,
  },

  // 카드
  card: {
    marginHorizontal: 16,
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    padding: 20,
    gap: 14,
    borderWidth: 1,
    borderColor: '#E5E7EB',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.08,
    shadowRadius: 6,
    elevation: 3,
  },

  // 섹션 헤더 (카드 내부)
  sectionHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  sectionIconBox: {
    width: 30,
    height: 30,
    borderRadius: 8,
    backgroundColor: '#EFF6FF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  sectionTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },

  // 플랫폼 카드
  platformCard: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 13,
    borderRadius: 12,
    borderWidth: 1.5,
    borderColor: '#E5E7EB',
    backgroundColor: '#FFFFFF',
    gap: 12,
  },
  platformCardSelected: {
    borderColor: '#5899FD',
    backgroundColor: '#EFF6FF',
  },
  platformIcon: {
    width: 38,
    height: 38,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  mattermostIcon: {
    backgroundColor: '#1A1A2E',
  },
  kakaoIcon: {
    backgroundColor: '#FEE500',
  },
  platformIconText: {
    fontSize: 17,
    fontWeight: '800',
    color: '#FFFFFF',
  },
  kakaoIconText: {
    color: '#3A1D00',
  },
  platformName: {
    flex: 1,
    fontSize: 15,
    fontWeight: '500',
    color: '#111827',
  },
  radioOuter: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#D1D5DB',
    alignItems: 'center',
    justifyContent: 'center',
  },
  radioOuterSelected: {
    borderColor: '#5899FD',
  },
  radioInner: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: '#5899FD',
  },

  // 메세지 편집
  sectionSubtitle: {
    fontSize: 13,
    color: '#9CA3AF',
    lineHeight: 20,
    marginTop: -6,
  },
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

  // 안내 박스
  infoBox: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    backgroundColor: '#F0F9FF',
    borderRadius: 10,
    padding: 12,
    gap: 8,
  },
  infoText: {
    flex: 1,
    fontSize: 12,
    color: '#0EA5E9',
    lineHeight: 19,
  },

  // 버튼
  buttonWrapper: {
    marginHorizontal: 16,
    marginTop: 4,
  },
});
