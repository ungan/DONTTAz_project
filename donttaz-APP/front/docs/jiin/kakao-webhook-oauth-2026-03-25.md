# 카카오톡 수치심 메세지 웹훅 OAuth 연동 계획

작성일: 2026-03-25

---

## 목적

위시리스트 금고의 긴급 출금 / 중도 해지 시, 사전 지정한 카카오톡 지인에게 수치심 메세지를 자동 발송한다.
이를 위해 `POST /api/v1/penalty-webhooks/{vaultId}` API에 카카오 OAuth 토큰 + 수신자 UUID를 전달해야 한다.

---

## API 스펙

**Endpoint:** `POST /api/v1/penalty-webhooks/{vaultId}`

**Request Body:**
```json
{
  "platformType": "KAKAOTALK",
  "webhookToken": "kakao_access_token",
  "refreshToken": "kakao_refresh_token",
  "expiresIn": 21599,
  "receiverUuids": ["uuid1", "uuid2"],
  "shameMessage": "저는 목표를 달성하지 못한 채 중도 포기했습니다."
}
```

**Response:** `{}`

---

## 환경 설정

### 프론트 .env에 추가 필요
```
EXPO_PUBLIC_KAKAO_CLIENT_ID=9a76a74b4b2925377f8a7901852d584f
```

### 카카오 개발자 콘솔 설정 필요
- [카카오 개발자 콘솔](https://developers.kakao.com) 접속
- 해당 앱 → **플랫폼 > Android** 에 패키지명 `com.hoein.front` 등록 확인
- **카카오 로그인 > Redirect URI** 에 다음 등록:
  - `front://oauth` ← APK/배포 환경
- **동의항목** 에서 다음 권한 활성화 필요:
  - `friends` (카카오톡 친구 목록 조회)
  - `talk_message` (카카오톡 메시지 전송)

> **주의:** Expo Go에서는 custom scheme(`front://`)이 동작하지 않는다.
> 반드시 `eas build -p android --profile preview`로 APK 빌드 후 테스트해야 한다.

---

## 구현 흐름

```
[수치심 메세지 설정 페이지]
        ↓
[카카오 로그인 버튼 클릭]
        ↓
WebBrowser.openAuthSessionAsync(
  "https://kauth.kakao.com/oauth/authorize?client_id=...&redirect_uri=front://oauth&response_type=code"
)
        ↓
[카카오 로그인 완료 → front://oauth?code=XXXX 로 리다이렉트]
        ↓
[인가 코드(code) 추출]
        ↓
POST https://kauth.kakao.com/oauth/token
  → access_token, refresh_token, expires_in 획득
        ↓
GET https://kapi.kakao.com/v1/api/talk/friends
  (Authorization: Bearer {access_token})
  → 친구 목록 (elements[].uuid, elements[].profile_nickname)
        ↓
[친구 선택 UI → receiverUuids 수집]
        ↓
POST /api/v1/penalty-webhooks/{vaultId}
  { platformType, webhookToken, refreshToken, expiresIn, receiverUuids, shameMessage }
        ↓
router.replace('/(tabs)/vault')
```

---

## 필요한 구현 목록

### 1. 환경 변수
- [ ] `.env`에 `EXPO_PUBLIC_KAKAO_CLIENT_ID` 추가

### 2. 카카오 개발자 콘솔
- [ ] Redirect URI `front://oauth` 등록
- [ ] `friends`, `talk_message` 동의항목 활성화

### 3. 토큰 교환 함수 (`entities/kakao/api/`)
- [ ] `exchangeKakaoCodeForToken(code)` → `{ access_token, refresh_token, expires_in }`
  - `POST https://kauth.kakao.com/oauth/token`
  - body: `grant_type=authorization_code&client_id=...&redirect_uri=front://oauth&code=...`

### 4. 친구 목록 조회 함수 (`entities/kakao/api/`)
- [ ] `getKakaoFriends(accessToken)` → `{ uuid, profile_nickname }[]`
  - `GET https://kapi.kakao.com/v1/api/talk/friends`

### 5. 친구 선택 UI (`features/vault-webhook/ui/`)
- [ ] 친구 목록 바텀시트 또는 모달
- [ ] 다중 선택 → `receiverUuids` 배열 구성

### 6. vault-webhook-page.tsx 수정
- [ ] "KakaoTalk 연동" 버튼 추가
- [ ] OAuth 플로우 연결
- [ ] 선택된 친구 표시
- [ ] 저장 버튼 → 전체 데이터 포함해서 API 호출

---

## 현재 상태 (2026-03-25)

- `POST /api/v1/penalty-webhooks/{vaultId}` API 함수 생성 완료 (`entities/vault/api/post-penalty-webhook.ts`)
- vaultId memorandum 페이지 → webhook 페이지로 라우팅 시 전달 완료
- `shameMessage` 입력 UI 완료
- **OAuth 연동 미구현** → `webhookToken` 빈 값으로 400 에러 발생 중
