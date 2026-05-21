# BuildOS Infra

Android Compose console for BuildOS fleet ops.

The app handles:
- login and session storage
- node inventory
- container control
- Cloudflare zones and DNS records
- Proxmox guest control
- emergency lockdown / reset
- live DNS lookup via Google resolver
- demo mode sandbox state

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

## Run Locally

Prerequisite: Android Studio.

1. Open the project in Android Studio.
2. Let Gradle sync and accept any import fixes.
3. Create `.env` in the repo root if you need local secrets. Use `.env.example` as the template.
4. Set `GEMINI_API_KEY` only if your environment or downstream features need it.
5. Run the `app` configuration on an emulator or physical device.

## Notes

- Debug signing config is already present in `app/build.gradle.kts`.
- The app shows a guest state until a token is saved.
- Live mode hits the configured base URL.
- Backend health and last sync time are shown in the app UI.
- Demo mode uses in-memory sandbox data and can seed live DNS lookups.

## Tests

- Local unit tests: `app/src/test`
- Robolectric tests: `app/src/test`
- Instrumented tests: `app/src/androidTest`
- Screenshot tests use Roborazzi
