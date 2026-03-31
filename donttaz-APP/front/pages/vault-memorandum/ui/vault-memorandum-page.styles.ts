import { StyleSheet } from 'react-native';

const HANDWRITTEN = 'serif';

export const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#FAFAF7',
  },
  scroll: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: 28,
    paddingTop: 16,
  },

  mainTitle: {
    fontSize: 40,
    fontWeight: '900',
    fontFamily: HANDWRITTEN,
    textAlign: 'center',
    color: '#1A1A1A',
    marginBottom: 28,
    letterSpacing: 4,
  },

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
    fontSize: 16,
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    lineHeight: 26,
    letterSpacing: 0.5,
    marginBottom: 32,
  },

  articleBlock: {
    marginBottom: 32,
  },
  articleTitle: {
    fontSize: 17,
    fontWeight: '700',
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    letterSpacing: 2,
    marginBottom: 12,
  },
  articleText: {
    fontSize: 15,
    fontFamily: HANDWRITTEN,
    color: '#1A1A1A',
    lineHeight: 26,
    letterSpacing: 1,
  },
  articleEmphasis: {
    fontWeight: '700',
    color: '#C0392B',
  },

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

  fadeOverlay: {
    position: 'absolute',
    bottom: 120,
    left: 0,
    right: 0,
    height: 60,
  },

  signButton: {
    backgroundColor: '#444444',
    height: 56,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 28,
    marginVertical: 32,
  },
  signButtonText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#FFFFFF',
    letterSpacing: 1,
  },
});
