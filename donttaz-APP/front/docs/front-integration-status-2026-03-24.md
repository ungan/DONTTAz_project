# Front Integration Status - 2026-03-24

## 1. Purpose

This document summarizes what is currently being verified on the real-device front flow and what should be checked next.

The main goal is to confirm that the front app, the local backend, and the iPhone device are connected correctly for:

- login
- signup identity verification
- burndown chart loading
- related real-server API flows

## 2. Current Goal

The current work is not a broad refactor.

It is a real-server integration check focused on these questions:

1. Does the iPhone app actually reach the backend?
2. Does signup move through the PortOne identity flow correctly?
3. Does login really call the backend and establish session state correctly?
4. Does the plan page automatically load burndown data from the backend?
5. Which errors are unrelated side issues and should be tracked separately?

## 3. Confirmed Findings

### 3.1 Login

Confirmed:

- the app can reach the login screen
- backend log shows `POST /api/v1/auth/login`
- login requests are reaching the backend

Implication:

- login is not blocked by a missing backend route
- earlier issues were more related to stored local auth state and runtime flow than to the login endpoint itself

### 3.2 Signup Identity Verification

Confirmed:

- signup starts from the front
- the front tries to open:

```txt
http://<device-reachable-ip>:8080/test-auth.html?redirectUri=front:///signup-start
```

- when the iPhone could not open that page, the auth session failed before backend verification
- later backend log shows `POST /api/v1/auth/verify-identity`

Implication:

- the verify endpoint exists and can be hit
- the unstable part is the device-to-backend web auth page access, not the existence of the backend verify API

### 3.3 Burndown Chart

Confirmed:

- front plan screen uses `useBurndown(yearMonth)`
- `useBurndown` automatically calls `GET /control/burndown`
- backend log shows:

```txt
GET /api/v1/control/burndown
```

- backend service log shows local dummy burndown fallback was returned when no spending plan existed

Implication:

- the burndown API is being called
- the backend endpoint exists
- burndown is not blocked by a missing API route

### 3.4 Separate 404 Issue

Confirmed:

- backend log shows:

```txt
GET /api/v1/ai/transactions/yearly
```

- this fails with `No static resource api/v1/ai/transactions/yearly`

Implication:

- this is a yearly report issue
- it is separate from the burndown chart flow

## 4. Important Current Endpoints

### 4.1 Burndown

- Front API path: `/control/burndown`
- Backend route exists
- Backend controller:
  - `donttaz-BE/api/src/main/java/com/donttaz/donttazapi/ai/controller/AiTransactionController.java`

### 4.2 Login

- Front API path: `/auth/login`
- Backend log confirms request handling

### 4.3 Signup Identity Verification

- Front web auth page:
  - `/test-auth.html`
- Front verification API:
  - `/auth/verify-identity`
- Backend log confirms verification request handling

### 4.4 Yearly Report

- Front API path:
  - `/ai/transactions/yearly`
- Current backend run log indicates this path is not mapped correctly in the active server

## 5. Current Device/Network Context

Current real-device testing uses an iPhone and a local backend exposed on a machine IP.

Important note:

- Expo Go tunnel connectivity and backend LAN/IP connectivity are separate
- even if Expo Go opens successfully, `test-auth.html` can still fail if the iPhone cannot access the backend host directly

Current base URL in front env:

```env
EXPO_PUBLIC_BASE_URL=http://172.20.10.8:8080/api/v1
```

## 6. Working Plan

### Step 1. Device-to-backend connectivity

Check whether the iPhone can directly open:

```txt
http://172.20.10.8:8080/test-auth.html
```

If this page does not open, signup identity flow will fail before `verify-identity`.

### Step 2. Signup flow validation

Confirm the full sequence:

1. signup start page opens
2. `test-auth.html` opens in auth session
3. callback returns with `identityVerificationId`
4. `/api/v1/auth/verify-identity` is called
5. next signup step opens

### Step 3. Login flow validation

Confirm the full sequence:

1. login button press
2. front calls `/api/v1/auth/login`
3. backend responds successfully
4. local session is stored correctly
5. app moves into authenticated flow

### Step 4. Burndown validation

Confirm the full sequence:

1. plan page opens
2. front automatically calls `/api/v1/control/burndown`
3. backend responds
4. chart renders real or fallback data

### Step 5. Separate unrelated API issues

Track these separately from burndown/signup:

- `/api/v1/ai/transactions/yearly` 404

## 7. Recommended Next Actions

Priority order:

1. verify device access to `test-auth.html`
2. re-run signup identity flow on device
3. verify login click-to-response flow
4. verify burndown chart rendering with returned backend data
5. investigate yearly report endpoint separately

## 8. Notes

- The currently running backend log came from a runtime under `BE_develop2`, so active runtime and current workspace should be compared carefully when debugging.
- Burndown endpoint handling is confirmed by both controller/service code and backend request logs.
- Yearly report 404 should not be mixed into burndown debugging.
