# 키보드 올라올 때 입력창 가려지는 문제

작성일: 2026-03-27

---

## 문제

TextInput이 있는 페이지에서 키보드나 바텀시트가 올라오면 입력창이 가려져서 현재 어떤 항목을 입력 중인지 알 수 없는 상황.

---

## 원인

React Native에서 키보드가 올라오면 기본적으로 레이아웃이 밀려 올라가지 않는다. `KeyboardAvoidingView`를 사용하지 않으면 입력창이 키보드 뒤에 그대로 가려진다.

---

## 해결 방법

`KeyboardAvoidingView`로 입력창이 포함된 영역을 감싼다.

- iOS: `behavior="padding"` → 컨텐츠 아래에 패딩을 추가해 키보드만큼 위로 밀어 올림
- Android: `behavior="height"` → 전체 높이를 줄여서 키보드 위 영역만 사용

```tsx
import { KeyboardAvoidingView, Platform } from 'react-native';

<KeyboardAvoidingView
  behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
  style={{ flex: 1 }}
>
  <ScrollView>...</ScrollView>
  <View>{/* 하단 버튼 */}</View>
</KeyboardAvoidingView>
```

---

## 적용 대상 페이지

| 페이지 | 파일 | 구조 |
|---|---|---|
| 수치심 메세지 설정 | `pages/vault-webhook/ui/vault-webhook-page.tsx` | SafeAreaView > **KAV** > PageHeader + ScrollView + buttonWrapper |
| 송금 (금액/메모) | `widgets/transfer-layout/ui/transfer-layout.tsx` | SafeAreaView > **KAV** > PageHeader + children |
| 로그인 | `pages/login/ui/login-page.tsx` | **KAV** (container 대체) > header + inputs + actions |
| 회원가입 계정설정 | `pages/signup-account/ui/signup-account-page.tsx` | View > headerBlock + **KAV** > ScrollView + bottomBar |
| 계좌개설 개인정보 | `pages/account-open-profile/ui/account-open-profile-page.tsx` | View > headerBlock + **KAV** > ScrollView + bottomBar |
| 계좌개설 전자금융 | `pages/account-open-banking/ui/account-open-banking-page.tsx` | View > headerBlock + **KAV** > ScrollView + bottomBar |

---

## 구조별 적용 패턴

### 패턴 A — SafeAreaView + ScrollView + 하단 버튼 고정

```tsx
<SafeAreaView edges={['bottom']} style={styles.container}>
  <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
    <PageHeader ... />
    <ScrollView ...>
      {/* 입력 필드 */}
    </ScrollView>
    <View style={styles.buttonWrapper}>
      <Button ... />
    </View>
  </KeyboardAvoidingView>
</SafeAreaView>
```

적용: `vault-webhook-page`, `transfer-layout`

### 패턴 B — 헤더 고정 + ScrollView + 하단 버튼 고정

```tsx
<View style={styles.outerContainer}>
  <View style={styles.headerBlock}>
    {/* 헤더는 KAV 밖 — 키보드 올라와도 고정 */}
  </View>
  <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={{ flex: 1 }}>
    <ScrollView ...>
      {/* 입력 필드 */}
    </ScrollView>
    <View style={[styles.bottomBar, { paddingBottom: insets.bottom + 16 }]}>
      <Button ... />
    </View>
  </KeyboardAvoidingView>
</View>
```

적용: `signup-account-page`, `account-open-profile-page`, `account-open-banking-page`

### 패턴 C — 전체를 KAV로 교체

```tsx
<KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : 'height'} style={styles.container}>
  {/* 기존 View 안의 내용 그대로 */}
</KeyboardAvoidingView>
```

적용: `login-page` (ScrollView 없이 단순 flex 레이아웃)

---

## 주의사항

- `SafeAreaView` 안에 `KeyboardAvoidingView`를 넣을 때, SafeAreaView는 `edges={['bottom']}`만 처리하도록 설정되어 있어야 이중 패딩이 발생하지 않는다.
- 헤더(`PageHeader`)는 KAV 안에 포함시켜야 키보드가 올라올 때 스크롤 영역과 하단 버튼만 압축되고 헤더는 유지된다. 단, 헤더가 별도 View로 분리된 패턴(B)에서는 헤더를 KAV 밖에 두는 것이 더 자연스럽다.
- `ScrollView`에 `keyboardShouldPersistTaps="handled"` 옵션을 함께 사용하면 키보드가 열린 상태에서 스크롤 영역을 탭해도 키보드가 닫히지 않는다.
