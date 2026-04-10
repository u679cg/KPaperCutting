---
name: kpappercutting-android-mvvm
description: Standardize code changes for the KPappercutting Android app. Use when Codex adds or edits Kotlin, Jetpack Compose, ViewModel, repository, model, theme, or navigation files under app/src/main so new work follows this project's Compose + MVVM feature structure, naming rules, state ownership boundaries, and file layout.
---

# KPappercutting Android MVVM

Follow this skill whenever a task changes the Android app structure or introduces new UI logic. Keep edits aligned with the repository's current direction: a single-module Compose app that is moving toward `Screen + ViewModel + Repository` per feature.

## Apply The Project Defaults

- Treat `app/src/main/java/com/example/kpappercutting/ui/features/<feature>/` as the unit of change for product features.
- Keep `Screen.kt` focused on rendering and wiring callbacks. Do not hide business state mutations inside composables when that state affects feature behavior.
- Put page state in `UiState.kt`, behavior and state transitions in `ViewModel.kt`, and data-source abstractions in `data/repository/`.
- Use `Route` composables, like `CreateRoute.kt`, to connect `viewModel()` and callbacks to a pure `Screen`.
- Place reusable pieces from a screen in `component/` once the screen starts mixing layout, controls, and logic-heavy blocks.

## Keep The Layer Boundaries Tight

- `data/model/` is for shared or persistent domain models.
- `ui/features/<feature>/...UiState.kt` is for screen-facing render state.
- `ViewModel` may translate repository models into UI state, but screens should not reach into repositories directly.
- Repositories may stay as interfaces or simple local stubs until real persistence/network work exists. Preserve the seam anyway.
- Keep app-wide shells such as [`AppSkeleton.kt`](G:\Project\AndroidStudioProjects\KPappercutting\app\src\main\java\com\example\kpappercutting\ui\AppSkeleton.kt) thin and feature-agnostic.

## Match Existing Naming

- Prefer filenames in these patterns:
  - `HomeScreen.kt`, `HomeViewModel.kt`, `HomeUiState.kt`
  - `CreateRoute.kt` when a route wrapper is needed
  - `SomethingRepository.kt` for data access seams
  - `SomethingUiModel` for view-facing list/item models inside a feature
- Use feature-local models before promoting types into `data/model/`.
- Preserve the current package layout instead of introducing cross-cutting folders like `widgets/`, `managers/`, or `helpers/` unless there is a clear app-wide need.

## Compose Guidance For This Repo

- Follow the visual and structural precedent already in the feature being edited. Do not impose a new design system in a small fix.
- Hoist meaningful state out of composables unless it is truly ephemeral preview-only UI state.
- Keep previews for leaf screens or components when practical, especially for new UI.
- Favor small extracted composables when a screen mixes multiple panels, toolbars, or content sections.
- Avoid introducing navigation frameworks or dependency injection frameworks unless the task explicitly calls for them.

## Default Workflow

1. Read the surrounding feature folder and [`MVVM_STRUCTURE_README.md`](G:\Project\AndroidStudioProjects\KPappercutting\MVVM_STRUCTURE_README.md) before changing structure.
2. Decide whether the change belongs in `Screen`, `Route`, `ViewModel`, `UiState`, `component`, `data/model`, or `data/repository`.
3. Make the smallest change that strengthens the existing architecture instead of bypassing it.
4. If a screen has grown large, extract UI blocks to `component/` before adding more inline complexity.
5. Run a focused validation command when possible, preferably `.\gradlew.bat :app:assembleDebug` or a narrower test task.

## Reference

- For the concrete folder map, feature checklist, and scaffolding expectations, read `references/project-architecture.md`.
- If the task creates a new feature, mirror an existing feature folder and adjust only what the new screen needs.
