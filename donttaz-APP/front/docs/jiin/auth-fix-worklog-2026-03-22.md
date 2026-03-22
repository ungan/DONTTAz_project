# 인증 버그 수정 워크로그 - 2026-03-22

## 1. 문제

`eas update` 후 Expo Go에서 앱을 실행하면 회원가입/로그인 화면 없이 바로 홈 화면으로 이동하는 문제.
Expo Go를 삭제 후 재설치해도 동일하게 발생.

---

## 2. 원인

### iOS Keychain 데이터 유지 특성

`expo-secure-store`는 내부적으로 iOS Keychain을 사용한다.
iOS Keychain은 **앱을 삭제해도 데이터가 유지**되는 특성이 있다.

```
이전 세션에서 로그인 → accessToken이 Keychain에 저장됨
      ↓
Expo Go 삭제 후 재설치
      ↓
Keychain의 accessToken은 그대로 남아있음
      ↓
앱 실행 → useAppEntryCheck()가 accessToken을 발견
      ↓
'authenticated' 반환 → 홈으로 이동 (로그인 화면 스킵)
```

Android는 앱 삭제 시 SecureStore 데이터도 함께 삭제되므로 해당 없음.

### PIN 화면도 안 뜨는 이유

PIN 화면 조건: `refreshToken && devicePin` 둘 다 존재해야 함.
이전 세션에서 PIN 설정 없이 로그인만 했다면 `devicePin`이 없으므로 PIN 화면 조건 불충족.
→ `accessToken`만 확인하는 두 번째 조건에 걸려 바로 홈으로 이동.

---

## 3. 해결 방법

### 아이디어

- `AsyncStorage`는 앱 삭제 시 초기화됨 (iOS도 동일)
- `SecureStore(Keychain)`은 앱 삭제해도 유지됨

→ AsyncStorage에 "설치됨" 플래그를 저장해 fresh install을 감지하고,
   플래그가 없으면(= 재설치) Keychain 토큰을 강제 삭제.

### 수정 파일

`features/auth/model/use-app-entry-check.ts`

### 수정 내용

```ts
const isInstalled = await AsyncStorage.getItem('app_installed');
if (!isInstalled) {
  await clearStoredAuthTokens();
  await clearStoredDevicePin();
  await AsyncStorage.setItem('app_installed', 'true');
}
```

토큰 확인 로직 실행 전에 위 코드를 추가.
최초 실행(또는 재설치) 시 Keychain에 남아있는 토큰을 삭제하고 플래그를 세운다.
이후 실행부터는 플래그가 존재하므로 기존 로직대로 동작.

---

## 4. 흐름 정리

```
앱 실행
  ↓
AsyncStorage에 'app_installed' 있음?
  → 없음(재설치): Keychain 토큰 전체 삭제 → 'app_installed' = 'true' 저장
  → 있음(기존 설치): 그대로 진행
  ↓
refreshToken + devicePin 있음? → PIN 로그인 화면
accessToken 있음?             → 홈 화면
없음?                         → 로그인 화면
```
