# BuildOS Infra

Android Compose console for BuildOS fleet ops.

The app handles:
- login and session storage
- runtime server URL switching
- node inventory
- container control
- Cloudflare zones and DNS records
- Proxmox guest control
- emergency lockdown / reset
- live DNS lookup via Google resolver
- demo mode sandbox state

## Current Behavior

- Guest mode shows the shell and probes backend health.
- Authenticated sessions poll fleet data in the background.
- Backend health and last sync time are shown in the UI.
- Demo mode uses in-memory sandbox data.
- Live DNS lookups are real HTTP queries to Google DNS.
- No client-side AI key is required by the Android app.

## Structure

- `app/src/main/java/com/example/MainActivity.kt` wires the UI and view model.
- `app/src/main/java/com/example/data/datastore/SessionManager.kt` stores token, base URL, username, role, and demo mode.
- `app/src/main/java/com/example/data/repository/InfraRepository.kt` owns API calls and demo sandbox state.
- `app/src/main/java/com/example/ui/viewmodel/InfraViewModel.kt` coordinates UI state and actions.
- `app/src/main/java/com/example/ui/screens/AllScreens.kt` contains the Compose screens.
- `app/src/main/java/com/example/ui/theme/*` defines the dark industrial theme.

## Build Notes

- `namespace`: `com.example`
- `applicationId`: `com.aistudio.buildosinfra.bdoinf`
- `compileSdk`: `36.1`
- `minSdk`: `24`
- theme: `Theme.MyApplication`
- default backend: `https://os.buildwithshashank.com`
- demo mode defaults off
- startup probes `GET /api/health`
- authenticated sessions poll fleet state in the background
- no WebSocket client is wired yet

## Run Locally

Prerequisite: Android Studio.

1. Open the project in Android Studio.
2. Let Gradle sync and accept any import fixes.
3. Run the `app` configuration on an emulator or physical device.

## Notes

- Debug signing config is already present in `app/build.gradle.kts`.
- The app shows a guest state until a token is saved.
- Live mode hits the configured base URL.
- Backend health and last sync time are shown in the app UI.
- Demo mode uses in-memory sandbox data and can seed live DNS lookups.
- The backend must expose the HTTPS APIs used by the app.
- Do not add client-side API keys back into the APK.

## Tests

- Local unit tests: `app/src/test`
- Robolectric tests: `app/src/test`
- Instrumented tests: `app/src/androidTest`
- Screenshot tests use Roborazzi

## Backend Contract

Current app calls these endpoints:
- `GET /api/health`
- `POST /api/auth/login`
- `GET /api/infra/nodes`
- `POST /api/infra/nodes/register`
- `PATCH /api/infra/nodes/:id`
- `DELETE /api/infra/nodes/:id`
- `POST /api/infra/nodes/:id/regenerate-token`
- `GET /api/infra/containers`
- `POST /api/infra/containers/:id/control`
- `POST /api/infra/containers/:id/auto-heal`
- `GET /api/infra/logs`
- `GET /api/cloudflare/zones`
- `POST /api/cloudflare/zones`
- `GET /api/cloudflare/dns?zone_id=...`
- `POST /api/cloudflare/dns`
- `DELETE /api/cloudflare/dns/:id`
- `GET /api/cloudflare/zones/:id/check`
- `POST /api/infra/emergency-kill`
- `POST /api/infra/emergency-reset`
- `GET /api/infra/nodes/:id/metrics`
- `GET /api/infra/nodes/:id/pve-guests`
- `POST /api/infra/nodes/:id/pve-guests/:kind/:vmid/control`
