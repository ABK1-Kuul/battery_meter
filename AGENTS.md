# AGENTS.md

This repository follows the UI/UX redesign specifications outlined in [UI_REDESIGN_PLAN.md](file:///c:/battery_meter/UI_REDESIGN_PLAN.md).

## Hard Rules (Apply to Every Task)

1. **DO NOT ADD COMMENTS** in Kotlin code (no `//`, no KDoc). This is an existing repo rule. Code must be self-explanatory through naming.
2. Do not modify `SRC/data/`, `SRC/domain/`, `SRC/service/`, `SRC/broadcast/`, `SRC/di/` or any `*ViewModel.kt` logic, except task D1. ViewModels may only receive additive, backward-compatible changes when a task says so.
3. Do not change dependency versions in `gradle/libs.versions.toml`. Do not add dependencies unless the task says so explicitly.
4. No hardcoded colors (`Color(0x...)`, `Color.Red`) and no hardcoded user-facing strings in composables. Colors come from `MaterialTheme.colorScheme` roles (this keeps dynamic color and the AMOLED mode working). Strings go in `app/src/main/res/values/strings.xml` and must also be added to `values-ko`, `values-fr`, `values-es`, `values-vi` (translate them).
5. No hardcoded `dp` or `RoundedCornerShape` in screens or feature components once tokens exist (T1). Use the token objects.
6. Keep all route constants, route names and `NavGraphBuilder` extension names unchanged.
7. Keep the architecture: Hilt, `@HiltViewModel`, `StateFlow` + `collectAsStateWithLifecycle`, `buildUiState`, `LocalRepositories`. Composables stay stateless where possible (state hoisting).
8. Every new or changed composable gets a `@Preview` (light, dark, and a large-font variant through the shared preview annotation from T5).
9. Do not add new entries to `app/lint-baseline.xml`.
10. Keep changes minimal per task. No drive-by refactors.

## Verification Commands

Run after each task:
```bash
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
./gradlew :app:testDebugUnitTest
```

Per-task validation checks:
- No hardcoded color literals: `grep -rn "Color(0x" app/src/main`
- No comments in changed Kotlin files: `grep -rnE "^\s*//|/\*\*" <changed .kt files>`
- All new strings translated in all 5 localization folders (`values`, `values-ko`, `values-fr`, `values-es`, `values-vi`).
- Previews configured for each composable (light, dark, 1.5x font scale).
