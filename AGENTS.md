# Repo Agent Rules

Use these rules for all edits in this repo.

## Style
- Use caveman style.
- Be concise.
- Code first.
- No fluff.

## App Shape
- Android Compose app.
- Main package is `com.example`.
- Core files:
  - `MainActivity.kt`
  - `InfraViewModel.kt`
  - `InfraRepository.kt`
  - `SessionManager.kt`
  - `AllScreens.kt`
  - `Models.kt`

## Behavior
- Login, node, container, DNS, Proxmox, and emergency flows all go through the view model and repository.
- Demo mode is in-memory sandbox state.
- Live mode uses the configured base URL and bearer token.
- Do not add secret logging or leak tokens in debug output.

## Workflow
- Inspect the relevant code path before editing.
- Prefer small, local edits.
- Do not revert unrelated user changes.
- Use `apply_patch` for manual file edits.
- Keep ASCII unless the file already uses non-ASCII.
- Update docs when behavior or setup changes.

## Android App
- Keep UI, data, and theme changes consistent with the existing structure.
- Verify any API, Gradle, manifest, or build changes against the current tree.
- If you touch auth or demo/live mode, check both `SessionManager` and `InfraRepository`.
