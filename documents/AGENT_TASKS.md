# Agent Tasks - Frontend Configuration And Loan Slider Fix

Purpose: this is the only active frontend task list. Older task batches are intentionally removed.

Read first:

- `documents/CLAUDE.md`
- `documents/PROJECT_STRUCTURE.md`
- `documents/API_SPEC.md`
- `documents/backend_contract.yaml`
- This file

Assume the implementation agent may only have the Android frontend repository. Backend source is not available to the frontend agent. Public demo base URL must be `https://easymoney.lamgd.dev/`.

## Source Issues Covered

| Issue | Main files to inspect | Expected outcome |
|---|---|---|
| Loan configuration amount bubble is too far from the slider and not aligned with the slider thumb on the X axis | `app/src/main/java/com/example/easymoney/ui/loan/configuration/LoanConfigurationContent.kt` | Amount bubble sits close above the track and its center follows the thumb center across min, middle, and max values |
| Removed language/mock/theme toggles do not guarantee the desired runtime config on devices with old persisted settings | `AppPreferences.kt`, `EasyMoneyApplication.kt`, `MainActivity.kt`, `NetworkModule.kt`, `SandBoxViewModel.kt`, `SandBoxScreen.kt`, `LocaleUtils.kt` | Fresh build always runs `REMOTE`, `vi`, `light`, public base URL, even if the installed app previously stored `MOCK`, `en`, dark mode, or a local URL |

## 1. Fix Loan Configuration Amount Bubble Alignment

Observed issue:

- In the loan flow configuration screen, the amount slider has a track/thumb and a component showing the current selected amount.
- The amount bubble is visually too far above the slider track.
- The amount bubble does not line up horizontally with the slider thumb.

Current likely root cause:

- `LoanAmountSection` places the bubble and the `Slider` inside a `Box` with fixed `height(104.dp)`.
- The bubble is drawn from the top of the `Box`, while the `Slider` is aligned with `Alignment.BottomCenter`, creating excessive vertical distance.
- The bubble offset is calculated from `sliderSize.width`, but `sliderSize` is the outer `Box` size, not the actual Material Slider thumb/track travel area.
- The current offset maps the bubble left edge across `(boxWidth - bubbleWidth) * progress`; it does not calculate the actual thumb center and then center the bubble on that point.
- Material3 `Slider` has internal horizontal space for the thumb, and the custom thumb is `24.dp`, so using full box width causes visible X-axis drift near min/max and sometimes mid-range.

Required fix:

- Keep the bubble close to the slider, with a small fixed gap such as `6.dp` to `8.dp` between the bubble pointer and the track/thumb area.
- Calculate bubble X position by centering it on the slider thumb center, then clamp the bubble inside the available width.
- Use the actual slider/thumb geometry instead of the outer `Box` height/width assumption.
- A safe implementation pattern:
  - measure the slider row width separately from the parent container;
  - define `thumbDiameter = 24.dp` to match the custom thumb;
  - compute `thumbRadiusPx = thumbDiameterPx / 2`;
  - compute `trackStartPx = thumbRadiusPx`;
  - compute `trackEndPx = sliderWidthPx - thumbRadiusPx`;
  - compute `thumbCenterPx = trackStartPx + progress * (trackEndPx - trackStartPx)`;
  - compute `bubbleLeftPx = (thumbCenterPx - bubbleWidthPx / 2).coerceIn(0f, sliderWidthPx - bubbleWidthPx)`;
  - apply `Modifier.offset(x = bubbleLeftPx.toDp())` to the bubble.
- Prefer replacing the tall free-positioned `Box` with a compact layout such as:
  - a `Box` for the bubble overlay;
  - `Spacer(6.dp)` or `Spacer(8.dp)`;
  - the `Slider`;
  - min/max labels.
- Do not let the bubble resize or shift the rest of the screen while dragging.
- Preserve the existing amount snapping behavior unless it directly causes the visual mismatch.

Acceptance:

- At min value, the bubble is near the left thumb and remains fully visible.
- At max value, the bubble is near the right thumb and remains fully visible.
- At mid value, the bubble center visually matches the thumb center.
- Vertical spacing between bubble pointer and slider is tight and intentional, not a large empty gap.
- The selected amount text still updates immediately while dragging.
- Build passes with `.\gradlew.bat build`.

## 2. Force Production Runtime Config Regardless Of Old Persisted Settings

Observed issue:

- UI toggles for language, mock/remote mode, and dark/light mode were removed from regular screens.
- Devices that previously used the app can still behave as if old config remains active.
- Desired production config for the next build is:
  - data source mode: `REMOTE`
  - language: `vi`
  - theme: `light`
  - API base URL: `https://easymoney.lamgd.dev/`

Current confirmed root cause:

- `AppPreferences.readDataSourceMode()` currently defaults to `DataSourceMode.MOCK`.
- `AppPreferences.dataSourceMode` still reads `KEY_DATA_SOURCE_MODE` from `SharedPreferences`, so an old installed app can keep `MOCK`.
- `AppPreferences.apiBaseUrl` still reads `KEY_API_BASE_URL`, so an old Sandbox/local URL can survive into a new build.
- `SandBoxViewModel` and `SandBoxScreen` can still write `dataSourceMode` and `apiBaseUrl`.
- `EasyMoneyApplication.forceVietnameseLocale()` already sets `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi"))`, but config enforcement should remain centralized and verifiable.
- `MainActivity` already calls `EasyMoneyTheme(darkTheme = false)`, but `darkThemeEnabled` can still hold stale data and any future caller could accidentally read it.

Required fix:

- Add a single production config policy in the frontend, preferably centralized in `AppPreferences` or a small dedicated constants object:
  - `PRODUCTION_DATA_SOURCE_MODE = DataSourceMode.REMOTE`
  - `PRODUCTION_API_BASE_URL = "https://easymoney.lamgd.dev/"`
  - `PRODUCTION_LANGUAGE_TAG = "vi"`
  - `PRODUCTION_DARK_THEME_ENABLED = false`
- Make getters used by production code return the forced values:
  - `dataSourceMode` must return `DataSourceMode.REMOTE` regardless of old `SharedPreferences`.
  - `apiBaseUrl` must return `https://easymoney.lamgd.dev/` regardless of old `SharedPreferences`.
  - `darkThemeEnabled` should return `false` or be made unused by production theme selection.
- On app startup, also write the forced values back to `SharedPreferences` once, so diagnostic screens and future code see the same values:
  - `KEY_DATA_SOURCE_MODE = REMOTE`
  - `KEY_API_BASE_URL = https://easymoney.lamgd.dev/`
  - `KEY_DARK_THEME_ENABLED = false`
- Keep `EasyMoneyApplication` forcing Vietnamese locale. If needed, expose a clear method such as `enforceProductionDefaults()` and call it before FCM registration or repository work that depends on `dataSourceMode`.
- Ensure `currentAppLanguage()` always resolves to `vi` for production behavior. Do not allow old app locale `en` to influence master-data API calls.
- Remove, disable, or make debug-only any Sandbox control that can write `MOCK` or a local `apiBaseUrl` in production. The safest quick fix is:
  - hide the data source and API base URL controls from production builds; or
  - make the Sandbox setters no-op unless an explicit debug flag is true.
- Do not clear auth tokens or user data as part of this config migration.
- Do not require users to uninstall the app or clear storage.

Recommended quick and safe approach:

1. Centralize production constants.
2. Make the critical getters return forced production values immediately.
3. Add a startup persistence repair method that overwrites only the config keys, not auth/profile/cache data.
4. Make Sandbox writes unable to override production config.
5. Keep `MainActivity` hard-coded light theme and `EasyMoneyApplication` hard-coded Vietnamese locale.

Acceptance:

- Install the new build over an app that previously stored `data_source_mode=MOCK`: repositories use `REMOTE`.
- Install the new build over an app that previously stored a local `api_base_url`: Retrofit uses `https://easymoney.lamgd.dev/`.
- Install the new build over an app that previously used English: app UI and master-data language are Vietnamese.
- Install the new build over an app that previously used dark mode: UI remains light.
- Sandbox or hidden debug screens cannot switch production runtime back to `MOCK` or a local URL.
- Existing login tokens and user data are not wiped by the config enforcement.
- Build passes with `.\gradlew.bat build`.

## Verification Checklist

Run from frontend root:

```powershell
.\gradlew.bat build
```

Manual verification:

- On a dirty existing install with old settings, launch the new build and verify network calls use the public remote backend.
- Confirm visible app text is Vietnamese.
- Confirm the app remains in light theme even if the device is dark or the previous app setting was dark.
- Open loan flow configuration and test amount bubble alignment at min, middle, and max.
