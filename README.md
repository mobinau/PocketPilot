# PocketPilot / پول‌یار

پول‌یار یک برنامه اندرویدی مدیریت مالی شخصی با رابط فارسی و راست‌به‌چپ است. درآمد و هزینه را ثبت کنید، بودجه تعیین کنید و الگوهای مالی خود را با گزارش‌ها و دستیار محلی بشناسید. برنامه کاملاً آفلاین کار می‌کند و برای حالت آزمایشی به کلید API نیاز ندارد.

PocketPilot is a Persian-first, offline-first Android personal finance application. It includes transaction management, monthly budgets, analytics, reports, local financial insights, and an on-device demo assistant. No backend or API key is required.

## Features / امکانات

- Four-page Persian onboarding, persisted with DataStore.
- RTL Material 3 interface, system Persian-capable typography, Persian digits and separators.
- Dashboard, income/expense entry, editing, deletion confirmation, search, category/type/date filters, transaction details.
- 18 Persian categories with icons. Optional monthly recurring transactions are materialized idempotently on app initialization and transaction save. Deleting a recurring template deletes its generated occurrences; deleting an individual occurrence does not stop its template.
- Monthly category budgets with editable limits and 80% / 100% warnings.
- Income/expense comparison, category donut chart, daily expense and savings trends, top expenses.
- Monthly reports, previous-month comparison, budget performance and deterministic local insights.
- Persian chat assistant with suggested questions and answers calculated from Room data.
- Light, dark and system themes; IRR, toman, USD and EUR preferences.
- Realistic first-launch demo data, explicit replacement confirmation and delete-all confirmation.
- No internet permission, no backend, no embedded secrets, no financial-data backup.

## Architecture

```
app/src/main/java/ir/pocketpilot/
  data/local/         Room entities, DAO, database, DataStore, demo seed, encrypted key store
  data/mapper/        Entity/domain conversions
  data/repository/    Offline Room repository and AI adapters
  domain/model/      Immutable models and monetary units
  domain/repository/ Repository contracts
  domain/usecase/    Financial analysis and exact amount parsing/formatting
  ui/                ViewModel, StateFlow and UI event channel
  ui/onboarding/     Introduction
  ui/navigation/     Navigation Compose graph
  ui/dashboard/      Overview
  ui/transactions/   Search, entry, detail and editing
  ui/budgets/        Monthly category limits
  ui/analytics/      Charts
  ui/reports/        Monthly report
  ui/ai/             Chat
  ui/settings/       Preferences and data actions
  ui/components/     Shared accessible UI components
  ui/theme/          Persian typography and Material 3 themes
```

MVVM with a small explicit application container provides dependencies without a heavyweight DI framework. Room flows combine into immutable financial data; the ViewModel produces reactive StateFlow state. Business calculations live outside Composables. Room writes and seed/reset operations use database transactions. Schema versions 1 and 2 are exported under `app/schemas`, with an explicit v1→v2 migration; future schema changes require migrations.

## Technology

Kotlin, Jetpack Compose, Material 3, Navigation Compose, lifecycle ViewModel, Room + KSP, Preferences DataStore, Coroutines / Flow, Gradle Kotlin DSL, JUnit and Compose instrumentation tests. AGP 9.1.1 uses built-in Kotlin 2.2.10. Minimum Android version: 8.0 (API 26), compile/target SDK: 36. Gradle: 9.3.1; JDK: 17.

## Monetary and date semantics

Amounts are stored as signed-64-bit positive integers: rials for IRR, cents for USD/EUR. Toman is a display/input unit for the same IRR ledger (1 toman = 10 rials). USD and EUR are separate ledgers; changing the currency never invents an exchange conversion. Fractions smaller than the storage unit and out-of-range values are rejected. Savings means net income minus expenses and may be negative.

Dates remain stored as epoch days (`LocalDate`). Financial periods use `FinancialMonth`, an explicit Persian year/month identifier such as `JALALI-1405-01`. Android's built-in ICU handles conversion, month lengths and leap Esfand; no additional runtime calendar library or handwritten conversion algorithm is used. Monthly budgets, reports, comparisons, demo insights, recurring transactions and month/year filters all use Persian periods. Weekly filtering starts on Saturday. Conversion and date display encode date fields in UTC without shifting an existing LocalDate through the device timezone; “today” retains the device's local civil-date semantics. The entry date picker currently uses a Gregorian grid with a Persian date headline.

Room version 2 migrates existing Gregorian budget labels to the Persian month containing the 15th of that legacy month. IDs, limits, categories and transactions are preserved; original month labels remain in metadata (`legacy_budget_period_<id>`). A full Gregorian month does not have an exact single Persian-month equivalent, so migrated limits can be reviewed/edited by the user. Existing recurring occurrences are preserved and a Persian month they already cover is not generated again. ICU4J is used **only in JVM tests** as the equivalent calendar implementation, never added to the APK. Android instrumentation tests exercise the actual built-in ICU and the v1→v2 migration.

No copyrighted font is downloaded; Android's Persian-capable system fallback is used.

## Run and build

1. Open this directory in a current Android Studio supporting AGP 9.1.1.
2. Install Android SDK platform 36 and build-tools 36.0.0; use JDK 17.
3. Configure `local.properties` with your SDK directory (the provided file points to this machine's `D:\Android-SDK` and is ignored by Git).
4. Sync Gradle and run the `app` configuration on an API 26+ device/emulator.

Windows PowerShell:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-17'
.\gradlew.bat testDebugUnitTest
.\gradlew.bat build
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest  # requires a running emulator/device
```

macOS/Linux:

```sh
chmod +x gradlew
./gradlew testDebugUnitTest build assembleDebug
./gradlew connectedDebugAndroidTest
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`. Debug signing is for local installation; a release distribution needs your own signing configuration.

Repositories include Google Maven, Maven Central and a Myket mirror fallback for environments with restricted repository access. An initial online sync is needed for dependencies not already cached.

## Demo mode and real AI integration

`AiAssistantRepository` accepts a question, immutable financial snapshot and selected currency. `DemoAiRepository` performs deterministic analysis locally. `ApiAiRepository` delegates to an injected `AiProvider`, so no fake network call is presented as real AI.

To integrate a provider later:

1. Implement `AiProvider.respond` using that provider's official SDK or HTTPS protocol, including timeouts, cancellation and Persian error handling.
2. Read the credential from `ApiCredentialStore` when configuring the provider; never log it or put it in source code. Settings stores keys encrypted with Android Keystore AES-GCM. An empty key field preserves an existing credential; the delete checkbox explicitly removes it.
3. Add internet permission and the required client dependency only when implementing actual networking.
4. Inject `ApiAiRepository(provider, calendar)` from the application container and add explicit provider selection/consent before transmitting financial context. The current build always uses demo mode, even if a future-provider name/key is stored.

## Tests

Unit tests cover balance, income, expense, savings, zero/negative cases, monthly report boundaries, currency isolation, budget thresholds, category spending, Persian amount handling, local insights and demo AI responses. Instrumentation tests cover Persian onboarding/navigation and Room seed/CRUD/reopen/clear behavior.

Current verification: successful build, 33 passing unit tests, 5 passing Android tests (including the corrected migration-test retry), and Lint with zero errors and 17 warnings. Manual checks verified launch, observed Persian RTL, Dashboard, Shamsi date, transaction creation/details and numeric keyboard. Remaining manual flows—including editing/deletion, filters, budgets, charts/reports, recurrence, Demo AI chat, settings, themes and restart persistence—are **Not verified manually**.

See [VERIFICATION.md](VERIFICATION.md) for evidence, the existing APK checksum and known limitations. Finalization changed documentation only; the successful build and tests were not repeated.
