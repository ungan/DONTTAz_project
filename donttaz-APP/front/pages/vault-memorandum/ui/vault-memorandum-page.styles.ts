import { StyleSheet } from 'react-native';

const HANDWRITTEN = 'serif';

export const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FAFAF7',
  },
  flex: {
    flex: 1,
  },
  scroll: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 28,
    paddingTop: 16,
  },

  // 제목
  mainTitle: {
    fontSize: 40,
    fontWeight: '900',
    fontFamily: HANDWRITTEN,
    textAlign: 'center',
    color: '#1A1A1A',
    marginBottom: 28,
    letterSpacing: 4,
  },

  // 유언자 정보
  userInfoSection: {
    marginBottom: 20,
    gap: 6,
  },
  userInfoText: {
    fontSize: 16,
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    letterSpacing: 2,
    lineHeight: 26,
  },

  divider: {
    height: 1.5,
    backgroundColor: '#1A1A1A',
    marginBottom: 20,
  },

  sectionSubtitle: {
    fontSize: 14,
    fontFamily: HANDWRITTEN,
    color: '#555555',
    lineHeight: 22,
    letterSpacing: 0.5,
    marginBottom: 24,
  },

  // 조항
  articleBlock: {
    marginBottom: 24,
  },
  articleTitle: {
    fontSize: 17,
    fontWeight: '700',
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    letterSpacing: 2,
    marginBottom: 8,
  },
  articleText: {
    fontSize: 15,
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    lineHeight: 24,
    letterSpacing: 1,
  },

  // 제1조 인라인 입력
  article1Row: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'baseline',
  },
  causeInput: {
    fontSize: 15,
    fontFamily: HANDWRITTEN,
    color: '#C0392B',
    letterSpacing: 1,
    borderBottomWidth: 1.5,
    borderBottomColor: '#C0392B',
    minWidth: 80,
    maxWidth: 160,
    paddingBottom: 1,
    paddingHorizontal: 2,
  },

  // 제2조, 제3조 멀티라인 입력
  multilineInput: {
    fontSize: 15,
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    lineHeight: 26,
    letterSpacing: 1,
    borderWidth: 0,
    borderBottomWidth: 1,
    borderBottomColor: '#CCCCCC',
    paddingVertical: 8,
    minHeight: 80,
  },

  // 서명 섹션
  signatureSection: {
    marginTop: 8,
    marginBottom: 32,
    alignItems: 'flex-end',
  },
  signatureName: {
    fontSize: 16,
    fontWeight: '700',
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    letterSpacing: 2,
    textDecorationLine: 'underline',
  },

  // 서명 버튼
  signButton: {
    backgroundColor: '#444444',
    height: 56,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  signButtonDisabled: {
    opacity: 0.4,
  },
  signButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
    letterSpacing: 1,
  },
});
