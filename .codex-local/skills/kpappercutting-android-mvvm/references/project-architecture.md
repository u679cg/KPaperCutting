# KPappercutting Project Architecture Reference

## Current Stack

- Android single-module app in `app/`
- Kotlin + Jetpack Compose
- AndroidX Lifecycle ViewModel
- Material 3
- Gradle Kotlin DSL with versions catalog in `gradle/libs.versions.toml`

## Source Layout

```text
app/src/main/java/com/example/kpappercutting/
├─ data/
│  ├─ model/
│  └─ repository/
├─ ui/
│  ├─ navigation/
│  ├─ theme/
│  ├─ features/
│  │  ├─ home/
│  │  ├─ culture/
│  │  ├─ creation/
│  │  ├─ community/
│  │  └─ profile/
└─ util/
```

## Layer Rules

- `MainActivity.kt` only hosts theme and top-level app entry.
- `ui/AppSkeleton.kt` coordinates top-level screen switching and shared chrome.
- `ui/features/<feature>/<Feature>Screen.kt` renders UI.
- `ui/features/<feature>/<Feature>Route.kt` connects a `ViewModel` to a pure screen when that split is useful.
- `ui/features/<feature>/<Feature>ViewModel.kt` owns screen state mutations and orchestration.
- `ui/features/<feature>/<Feature>UiState.kt` defines render-ready state.
- `ui/features/<feature>/component/` holds reusable subviews for that feature.
- `data/repository/` defines data seams even if implementation is still fake or in-memory.
- `data/model/` holds shared domain data rather than one-off UI-only view data.

## Naming Rules

- Keep one feature per folder under `ui/features/`.
- Use singular feature prefixes consistently inside the folder, for example `CreateScreen`, `CreateUiState`, `CreateViewModel`.
- Reserve `UiModel` suffixes for feature-facing card/list/panel items.
- Reserve `Repository` suffixes for data access interfaces or implementations.
- Use `Route` only when a composable bridges `viewModel()` and a pure screen.

## State Rules

- Put durable screen state in `ViewModel`, not in nested composables.
- Allow local composable state only for short-lived visual details that do not affect feature behavior outside that composable.
- Translate user gestures into explicit actions or callbacks rather than mutating unrelated objects deep in the UI tree.
- If the screen already has an action type, extend it instead of adding ad hoc lambdas for stateful behavior.

## Compose Rules

- Keep screen files readable by extracting large visual sections into `component/`.
- Add `@Preview` for new screens or notable reusable components when practical.
- Follow existing color, typography, and spacing tokens before introducing new ones.
- Prefer clear, feature-local composables over generic abstractions introduced too early.

## New Feature Scaffold

When creating a new feature, default to this file set:

```text
ui/features/<feature>/
├─ <Feature>Screen.kt
├─ <Feature>UiState.kt
├─ <Feature>ViewModel.kt
└─ component/            # only if the screen is already large
```

Add `<Feature>Route.kt` when:

- the screen should stay stateless or preview-friendly
- `viewModel()` setup would otherwise live inside the screen
- menu/back/navigation side effects need a thin adapter layer

Add `data/repository/<Feature>Repository.kt` when:

- the feature loads or saves data
- fake data is likely to become a real source later
- multiple screens may eventually share that data access logic

## Review Checklist

Before finishing a change, check:

1. Did the change stay inside the most specific feature folder possible?
2. Did UI rendering stay separate from state mutation?
3. Did any new state get placed in `ViewModel` or `UiState` when it affects feature behavior?
4. Did reusable screen sections move into `component/` instead of inflating the screen file further?
5. Did any new data dependency get introduced through a repository seam instead of directly from UI?
6. Did naming stay consistent with the rest of the project?
