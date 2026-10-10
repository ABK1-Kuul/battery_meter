# Battery Meter: UI/UX Redesign, Agent Implementation Plan

Audience: autonomous coding agents (Antigravity, Codex, Claude Code). Humans review PRs.
Package: `io.github.ifa.glancewidget`. Source root: `app/src/main/java/io/github/ifa/glancewidget/` (called `SRC/` below).

## 0. How to run this plan

Give each agent ONE task at a time using this kickoff prompt:

> Read `AGENTS.md` and `UI_REDESIGN_PLAN.md` fully. Implement task `<ID>` only. Follow section 1 (hard rules) exactly. Do not start other tasks. When done, run the verification commands in section 7, then report: files changed, commands run with pass/fail, anything you could not verify.

Rules for the process:
- One task = one branch (`ui/<ID>-<slug>`) = one commit series. The build must pass after every task.
- Tasks run in the order of section 5. A task may start only when its dependencies are merged.
- If an Android SDK or Gradle is unavailable in your environment, say so explicitly. Never claim a build, lint or test passed unless you ran it.

## 1. Hard rules (apply to every task)

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

## 2. Verified repo facts

- Compose BOM `2026.01.01`, Material 3 `1.5.0-alpha15` (Expressive APIs, experimental), Glance `1.2.0-alpha01`, Vico `2.0.0-beta.1`, Navigation Compose `2.9.3`, Kotlin `2.0.10`, AGP `8.12.1`, compile/target SDK 36, min SDK 26.
- Already used and must be reused, not replaced: `HorizontalFloatingToolbar` (bottom bar), `CircularWavyProgressIndicator`, `SharedTransitionLayout` (`LocalSharedTransitionScope`), `AnimatedCounter`, `RecompositionCounter`, `material3color` module (`rememberColorScheme`, Glance color scheme), Google Sans Flex fonts (`R.font.google_sans_flex_400/600`), `kotlinx.collections.immutable`.
- Tabs live in `SRC/features/main/MainFloatBottomBar.kt` (`MainScreenTab`: Widget, Health, Settings). `appUsageScreen()` is already registered in the main nav graph (`appUsageScreenRoute`) but has no tab.
- Screens duplicate the same `Scaffold` + `TopAppBar` + `LazyColumn` + `appPadding()` + `topBarColors` pattern (`BatteryMonitorWidgetScreen.kt`, `HealthScreen.kt`, `SettingsScreen.kt`, `AppUsageScreen.kt`).
- Current surface hierarchy is inverted: pages use `topBarColors.containerColor` (`surfaceContainer`) while cards use `colorScheme.background`.
- Home tiles are fixed-height boxes (100/120/140/180 dp). Add Widget and AOD buttons are 80 dp tall. The AOD toggle is a `FilledIconToggleButton` with hardcoded "AOD" text.
- `Type.kt` uses `googleFlex600` for display, headline, titleMedium/Small and bodyLarge; the rest use 400. All styles set `fontFeatureSettings = "ss02, dlig"`.
- Widget: `BatteryWidget.Content` renders only `Style.Horizontal` and `Style.Circle`. `Style.Vertical` exists in the enum and falls into `else -> {}`. The widget picker offers Circle and Horizontal (opaque and transparent).
- Existing tests: only `ExampleUnitTest.kt`. `junit` 4.13.2 is available for JVM tests.

## 3. Design specification

### 3.1 Direction
Calm, data-first, Material 3 Expressive. One hero per screen. Large soft radii. Tonal surfaces, not borders or shadows. Color communicates battery state. Numbers are large and use tabular figures.

### 3.2 Tokens (T1)

```kotlin
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screenHorizontal = 16.dp
}

object AppShape {
    val hero = RoundedCornerShape(36.dp)
    val card = RoundedCornerShape(28.dp)
    val inner = RoundedCornerShape(20.dp)
    val chip = CircleShape
}

object AppMotion {
    const val short = 200
    const val medium = 350
    fun <T> spatial() = spring<T>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
    fun <T> effects() = tween<T>(durationMillis = short)
}
```

### 3.3 Typography (T2)
Edit `SRC/ui/theme/Type.kt`:
- display*, headline*, titleLarge, titleMedium, titleSmall: `googleFlex600`, keep `"ss02"`.
- body*, label*: `googleFlex400`. `labelLarge` (buttons): `googleFlex600`.
- Remove `dlig` from body and label roles.
- Add a `MetricNumber` text style set (extension vals on `Typography`: `metricLarge` 56sp, `metricMedium` 28sp, `metricSmall` 18sp), `googleFlex600`, `fontFeatureSettings = "tnum, ss02"`. Used for every live number.

### 3.4 Surfaces
- Page background: `colorScheme.surface`.
- Cards and tiles: `surfaceContainerLow`. Raised or grouped content: `surfaceContainerHigh`.
- Hero card: tone container color (3.5).
- AMOLED mode: if cards are not visually distinct from the black page, use `surfaceContainer` instead. Verify in previews.

### 3.5 Battery tone (state-driven color)

Priority order, first match wins:

| Tone | Condition | container / onContainer / accent |
|---|---|---|
| Hot | temperature >= 42 C, or health is overheat | errorContainer / onErrorContainer / error |
| Low | level <= 15 and not charging | errorContainer / onErrorContainer / error |
| Full | level >= 100 | secondaryContainer / onSecondaryContainer / secondary |
| Charging | isCharging | tertiaryContainer / onTertiaryContainer / tertiary |
| Cold | temperature <= 5 C | secondaryContainer / onSecondaryContainer / secondary |
| Normal | otherwise | primaryContainer / onPrimaryContainer / primary |

### 3.6 Motion and accessibility
- Wavy ring animates (wave) only while charging; no other infinite animations.
- Numbers change with `AnimatedCounter`. Tile to detail uses the existing shared-element transition.
- Touch targets >= 48 dp. Text contrast per Material roles (do not tint text with alpha below 0.7).
- Hero has merged semantics: "Battery 82 percent, charging, 32 minutes to full".
- Everything must remain usable at 1.5x font scale (no clipped text; use `heightIn(min = ...)`, never fixed `height` for text containers).

## 4. Target architecture

```
SRC/ui/
  theme/                  Theme.kt, Type.kt, Color.kt (existing, edited in T2/T3)
  designsystem/
    tokens/               Spacing.kt, AppShape.kt, AppMotion.kt
    components/           AppScaffold, HeroBattery, MetricTile, Sparkline, StatusChip,
                          SectionHeader, SettingsGroup, SettingsRow, InfoBanner, PreviewAnnotations
  model/                  BatteryTone.kt, UiText.kt, BatteryUiModel.kt
SRC/features/*            existing screens, rebuilt only from designsystem components
SRC/glance/battery/       widget composables, colors resolved from BatteryTone
```

Data flow (unchanged ViewModels): `Repository -> UseCase -> ViewModel UiState -> (pure mapper) BatteryUiModel -> stateless composables`.

The mapper is a pure Kotlin function (unit-testable on the JVM):

```kotlin
fun BatteryDataWrapper.toUiModel(temperatureUnit: MyDevice.Temperature.TemperatureUnit?): BatteryUiModel
```

## 5. Task list and order

| ID | Task | Depends on |
|---|---|---|
| T0 | Agent entry files | none |
| T1 | Design tokens | T0 |
| T2 | Typography roles | T1 |
| T3 | Surfaces and `AppScaffold` | T1 |
| T4 | `BatteryTone`, `UiText`, `BatteryUiModel` + unit tests | T1 |
| T5 | Shared components + preview annotations | T2, T3, T4 |
| T6 | Home screen | T5 |
| T7 | Health screen | T5 |
| T8 | Settings screens | T5 |
| T9 | App Usage screen + 4th tab | T5 |
| T10 | Adaptive navigation | T9 |
| T11 | Widget visuals | T4 |
| T12 | Widget picker live preview | T11 |
| D1 | (optional, data layer) timestamped history | none |
| T13 | Detail chart with range chips | T6, D1 |
| T14 | Polish: motion, a11y, perf | T6-T12 |
| T15 | (optional) screenshot tests | T14, needs human approval for new dependency |

## 6. Task specifications

### T0 Agent entry files
- Create `AGENTS.md` at repo root: section 1 rules, build/verify commands from section 7, a pointer to this plan.
- Create `CLAUDE.md` and fill the empty `GEMINI.md`, each containing only: `See AGENTS.md`.
- Do not touch `.agent/skills/`.
- Done when: three files exist, no code changed.

### T1 Design tokens
- Create `SRC/ui/designsystem/tokens/Spacing.kt`, `AppShape.kt`, `AppMotion.kt` exactly as in 3.2.
- Done when: project compiles, tokens unused so far.

### T2 Typography
- Edit `SRC/ui/theme/Type.kt` per 3.3. Keep the `Type.typography` entry point.
- Add `metricLarge/Medium/Small` as `val Typography.metricLarge: TextStyle` extensions in the same file.
- Done when: a preview shows all roles; bodies are visibly lighter than titles.

### T3 Surfaces and AppScaffold
- Create `SRC/ui/designsystem/components/AppScaffold.kt`:

```kotlin
@Composable
fun AppScaffold(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    snackbarHost: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
)
```
- It wraps the existing `TopAppBar` pattern (collapsing, centered title, `headlineLarge`), uses `colorScheme.surface` for container and scrolled container, and applies `Spacing.screenHorizontal` to content.
- Keep `topBarColors` and `appPadding()` in place (still used until T6-T9 finish), but do not use them in new code.
- Done when: preview renders; nothing else changed.

### T4 Tone, UiText, UiModel
- Create `SRC/ui/model/BatteryTone.kt` with `enum class BatteryTone { Normal, Charging, Full, Low, Hot, Cold }`, a pure `fun resolveBatteryTone(level: Int, isCharging: Boolean, temperatureCelsius: Float, isOverheat: Boolean): BatteryTone` following the table in 3.5, and a `@Composable fun BatteryTone.colors(): ToneColors` (data class `container`, `onContainer`, `accent`) reading only `MaterialTheme.colorScheme`.
- Create `SRC/ui/model/UiText.kt`: `sealed interface UiText` with `Plain(value: String)` and `Resource(@StringRes id: Int, args: List<Any>)`, and `@Composable fun UiText.asString(): String`.
- Create `SRC/ui/model/BatteryUiModel.kt`:

```kotlin
@Immutable
data class BatteryUiModel(
    val level: Int,
    val tone: BatteryTone,
    val statusLine: UiText,
    val chargerChip: UiText?,
    val powerText: String,
    val temperatureText: String,
    val voltageText: String,
    val currentText: String,
    val isCharging: Boolean
)
```
- Create the mapper `toUiModel` (pure, no Android `Context`). Reuse existing strings `charging`, `discharging`, `remain_time_charging`, `remain_time_battery`. Time text comes from the existing `getBatteryTimeRemaining`/`getChargeTimeRemaining` values formatted via a passed-in formatter lambda `(Long) -> String`.
- Create `app/src/test/java/io/github/ifa/glancewidget/ui/model/BatteryToneTest.kt` covering: every row of the table, priority conflicts (hot + charging = Hot, low + hot = Hot, full + charging = Full), boundary values (15/16 percent, 41.9/42 C, 5/5.1 C).
- Done when: `:app:testDebugUnitTest` passes with the new tests.

### T5 Shared components
Create in `SRC/ui/designsystem/components/`, each stateless, each with previews:

```kotlin
@Composable fun HeroBattery(model: BatteryUiModel, chips: List<ChipData>, modifier: Modifier = Modifier)
@Composable fun MetricTile(label: String, value: String, unit: String, icon: ImageVector, modifier: Modifier = Modifier, series: ImmutableList<Float>? = null, accent: Color = MaterialTheme.colorScheme.primary, onClick: (() -> Unit)? = null)
@Composable fun Sparkline(values: ImmutableList<Float>, color: Color, modifier: Modifier = Modifier)
@Composable fun StatusChip(label: String, icon: ImageVector?, modifier: Modifier = Modifier)
@Composable fun SectionHeader(text: String, modifier: Modifier = Modifier)
@Composable fun SettingsGroup(title: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit)
@Composable fun SettingsRow(title: String, modifier: Modifier = Modifier, subtitle: String? = null, leadingIcon: ImageVector? = null, trailing: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null)
@Composable fun InfoBanner(text: String, icon: ImageVector, modifier: Modifier = Modifier, progress: Float? = null, actionLabel: String? = null, onAction: (() -> Unit)? = null)
```
- `HeroBattery`: `AppShape.hero`, container = tone container; centered `CircularWavyProgressIndicator` (reuse the existing component usage from `BatteryExtraInformation`), level in `metricLarge` with `AnimatedCounter`, `statusLine` below, chips in a `FlowRow`. Wave animation only when `model.isCharging`. Merged semantics per 3.6.
- `Sparkline`: draws with `Canvas` and a `Path` (no Vico). Auto-scales to the data min/max with 10 percent padding. Handles 0, 1 and N points. Stroke 2 dp, rounded caps.
- `MetricTile`: `AppShape.card`, `surfaceContainerLow`, `heightIn(min = 112.dp)`, value in `metricMedium`, unit in `labelMedium`, sparkline at bottom when provided. Parameters are primitives or immutable so a changing neighbor tile does not recompose it.
- `SettingsGroup`: `AppShape.card` container, rows separated by inset dividers (no full-width lines).
- `PreviewAnnotations.kt`: a multi-preview annotation covering light, dark, and `fontScale = 1.5f`.
- Done when: every component has previews, no hardcoded color or dp literals except local layout constants like icon sizes.

### T6 Home screen
Files: `SRC/features/battery/BatteryMonitorWidgetScreen.kt`, `features/battery/component/BatteryOverall.kt`, `MeasurementWarning.kt`, `WattsMonitor.kt`, `ConnectedDevice.kt`, `BonnedDeviceItem.kt`, `DropdownMenu.kt`, `AddWidgetBottomSheet.kt`.

Layout, top to bottom, inside `AppScaffold` and a `LazyColumn` with `Spacing.l` spacing:
1. `InfoBanner` for measurement progress (only when `measurementProgress < 100f`), replacing the current warning card.
2. `HeroBattery` from `uiState.batteryOverall.toUiModel(...)`. Chips: charger type, power in watts, temperature.
3. 2-column grid of `MetricTile`: Power, Current, Temperature, Voltage. Temperature and voltage tiles show `Sparkline` from `uiState.chartTrackingData` (convert temperature to the selected unit exactly as the existing code does). Power tile click still calls `onOpenWattsDetailScreen`.
4. Paired devices section (`SectionHeader` + existing items restyled) when present.
5. Actions: move "Add widget" into the top bar `actions` (icon button with content description) and show AOD as a `SettingsRow`-style card at the bottom of the list. Remove the two 80 dp buttons. Replace the hardcoded "AOD" text and content description with a string resource.
- Behavior must not change: same ViewModel calls, same navigation callbacks, same bottom sheet flow.
- Done when: no fixed-height text containers remain; at 1.5x font nothing clips; only the changed tile recomposes on a 1 s current update (verify with `RecompositionCounter` in a debug build).

### T7 Health screen
Files: `SRC/features/health/HealthScreen.kt`, `component/BatteryExtraInformation.kt`.
- Hero: health score ring (existing wavy indicator logic) with the percentage and a verdict `StatusChip` from `batteryHealth`.
- Replace the 2/3-column grid with two `SettingsGroup`s: Capacity (design, full charge, charge counter) and Condition (health, technology, cycle count). Values use `metricSmall`.
- Keep the `asMah()` dash behavior for missing readings.
- Done when: the screen uses only `AppScaffold`, `SettingsGroup`, `SettingsRow`, `StatusChip`.

### T8 Settings
Files: `SRC/features/settings/**`.
- Group into Appearance, Notifications, Widget, About with `SettingsGroup`/`SettingsRow`.
- Theme picker shows live color swatches (`PaletteItem` restyled with `AppShape.inner`).
- No behavior change in `SettingsViewModel`.
- Done when: every row is a `SettingsRow`; no screen-local card styling remains.

### T9 App Usage and 4th tab
Files: `SRC/features/main/MainFloatBottomBar.kt`, `SRC/features/appusage/**`.
- Add `Usage(appUsageScreenRoute, R.string.app_usage, selectedIcon, icon)` to `MainScreenTab` between Health and Settings. Icons from `material-icons-extended` or new vector drawables.
- Restyle list: app icon, name, duration (`metricSmall`), proportional bar relative to the top app. Permission-missing state becomes an `InfoBanner`-based card with the existing grant action.
- Done when: four tabs render, selection state works, the bar does not overflow at 1.5x font.

### T10 Adaptive navigation
- In `MainScreen.kt`, use a `NavigationRail` layout when `windowSizeClass` width is at least `WIDTH_DP_MEDIUM_LOWER_BOUND`, keep the floating toolbar otherwise. Use the existing `libs.androidx.compose.adaptive.suite` dependency.
- Done when: previews at 360 dp, 700 dp and 1000 dp widths look correct.

### T11 Widget visuals
Files: `SRC/glance/battery/**`.
- Resolve widget accent colors from `BatteryTone` through the Glance color scheme in `material3color` (no hardcoded colors).
- Restyle `Horizontal` and `Circle`: larger level text, tone-colored progress, consistent 24 dp corner radius (`cornerRadiusCompat`), same transparent behavior.
- `WidgetSetting.Style.Vertical`: either implement a simple vertical variant and add it to the picker, or remove it from the enum and every `when`. Implement it only if `AddWidgetBottomSheet` is updated in T12. Otherwise remove it.
- Glance constraints: no gradients, no animation.
- Done when: all `@GlancePreview`/preview sizes (1x1, 2x1, 4x1, 2x2, 4x4) render without clipping.

### T12 Widget picker live preview
- In `AddWidgetBottomSheet.kt`, show a live preview of the selected style and transparency using the same composables as the widget. Style selection becomes `SelectableChips`-based or segmented buttons.

### D1 (optional) Timestamped history
Allowed exception to rule 2, data layer only.
- Add `timestamps: List<Long> = emptyList()` to `ChartRecord` (backward compatible serialization), record `System.currentTimeMillis()` in `saveHistoryForChart` next to each sample, keep the 100-sample cap.
- Done when: old stored JSON still deserializes (add a unit test with a legacy JSON string).

### T13 Detail chart
- New destination (type-safe route, same pattern as `WattsDetailDestination`) opened by tapping the Temperature or Voltage tile, using the shared-element transition. Full Vico line chart with axes, marker, and range chips (1h, 6h, 24h) filtered by timestamps. Values in the selected temperature unit.
- Isolate Vico behind one composable so the beta API is replaceable.

### T14 Polish
- Audit every screen against section 3.6 and rule 1 to 5 with grep: no `Color(0x`, no `RoundedCornerShape(` outside `AppShape.kt`, no `.dp` literals for spacing in `features/`.
- Add `semantics` where missing, verify TalkBack order on Home.
- Verify recomposition behavior on Home with `RecompositionCounter`.
- Remove unused `topBarColors` and `appPadding()` if no references remain.

### T15 (optional) Screenshot tests
Requires human approval for a new dependency (suggested: Roborazzi). Record light, dark, AMOLED and 1.5x font for Home, Health, Settings, Usage.

## 7. Verification protocol (every task)

```
./gradlew :app:assembleDebug
./gradlew :app:lintDebug
./gradlew :app:testDebugUnitTest
```

Plus, per task:
- Search for violations: `grep -rn "Color(0x" app/src/main` and `grep -rnE "^\s*//|/\*\*" <changed .kt files>` must return nothing for new code.
- All new strings exist in the five `strings.xml` files.
- Previews render for every changed composable (light, dark, large font).
- Report anything you could not run. Do not claim success for unrun checks.

## 8. Risks and stop-and-ask triggers

Stop and ask a human before proceeding if:
- A required Material 3 Expressive or Glance alpha API does not exist in the pinned version. Do not bump versions to work around it; propose an alternative using stable APIs.
- A task seems to require changing a ViewModel contract, repository, or data model (other than D1).
- AMOLED contrast cannot be made acceptable with surface roles alone.
- Vico beta behaves unexpectedly with the detail chart. Fallback: draw the detail chart with `Canvas` like `Sparkline`.
- A change would remove a user-visible feature.

Known risks: pre-release UI libraries, Glance rendering limits, recomposition cost of per-second updates, and translations for new strings in four languages.

## 9. Definition of done (whole redesign)

- All screens are built only from `designsystem` components and tokens.
- Zero hardcoded colors; zero literal shapes or spacing outside tokens.
- Home, Health, Settings, Usage verified at 360 dp and 700 dp+, light, dark, AMOLED, 1.5x font.
- Widget styles shipped match the picker options; no unrendered style remains in the enum.
- Build, lint and unit tests pass; `BatteryToneTest` covers the tone table.
