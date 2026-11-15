# Explore/Home Phase 1

Scope: Ravitejas, `feature/explore`. Original PDF and assignment image were inspected visually; every handoff source was read before cloning or implementation. No Phase 2 work was started.

## Contracts and extraction

The old `ExploreDynamicScreen` and `CountryCardItem` were embedded in `AppNavGraph.kt`. Their screen/card responsibilities were extracted into `ui/home/` and `ui/components/`; there is one Explore implementation. Navigation now invokes `HomeScreen` and retains all existing routes, argument names, and downstream screens.

The real model is REST Countries v5: `names.common`, `codes.alpha3`, `flag.urlPng`, and `region`. It is not the v3.1 sample model in the team plan. The existing `country_details/{countryCode}` route receives Alpha-3, e.g. `IND`. `Country.detailsCode()` trims and normalizes a three-letter code; a missing or malformed identifier disables the card instead of navigating to a made-up country.

## Behavior

- Shared Material3 theme, Scaffold and LazyColumn.
- Reusable country cards, country-name search field and horizontally scrollable region chips.
- Country common-name substring search ignores case and trims surrounding whitespace. It does not search region names.
- Regions: All, Africa, Americas, Asia, Europe, Oceania. Region matching is exact and ignores case. Search AND region must match.
- ViewModel holds loading/content/error state and calls the existing suspending repository. Retry prevents concurrent duplicate loads. Filter changes do not make network requests.
- Query and region use `rememberSaveable`, retaining them on back navigation and state restoration. No SharedPreferences added.
- Distinct loading, retryable load failure, retryable empty dataset, and no-filter-matches states. Reset filters clears both controls; clear search retains the region.
- Glide loads PNG flags with a loading/error placeholder and aspect-preserving Fit. SVG-only records show the fallback because the existing Glide setup has no SVG decoder.
- UI text is in Android string resources. Raw network error text is not displayed.

No production fixture data, new library, Gradle version change, API migration, map change, wishlist, trip, or persistence feature was added.

## Files

| File | Purpose |
| --- | --- |
| `app/src/main/java/com/pes/travelexplorer/ui/home/HomeScreen.kt` | Repository-backed screen wrapper, UI states, saved search/filter controls |
| `app/src/main/java/com/pes/travelexplorer/ui/home/HomeViewModel.kt` | Repository-backed state and retry lifecycle |
| `app/src/main/java/com/pes/travelexplorer/ui/home/CountryFilters.kt` | Combined predicate, supported regions, Alpha-3 validation |
| `app/src/main/java/com/pes/travelexplorer/ui/components/CountryCard.kt` | Reusable Glide card and safe selection |
| `app/src/main/java/com/pes/travelexplorer/ui/components/CountrySearchBar.kt` | Search and clear action |
| `app/src/main/java/com/pes/travelexplorer/ui/components/RegionFilter.kt` | Selected region chips |
| `app/src/main/java/com/pes/travelexplorer/ui/cities/CityScreen.kt` | Small authorized fix: observe the configured locale when formatting population |
| `app/src/main/java/com/pes/travelexplorer/ui/navigation/AppNavGraph.kt` | Replace embedded Explore with HomeScreen; preserve route contract |
| `app/src/main/res/values/strings.xml` | Explore copy and accessibility labels |
| `app/src/test/java/com/pes/travelexplorer/ui/home/CountryFiltersTest.kt` | Filter/identity edge cases |
| `app/src/androidTest/java/com/pes/travelexplorer/ui/home/HomeScreenTest.kt` | Fixture UI and retry/state restoration tests |
| `app/src/androidTest/java/com/pes/travelexplorer/ui/home/ExploreNavigationTest.kt` | Actual live app search, region, details and back flow |
| `docs/phase1-requirements.md` | Evidence matrix prepared before source edits |
| `docs/phase1-explore.md` | Implementation, verification and viva handoff |

## Verification

| Check | Actual result |
| --- | --- |
| `bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest --console=plain` | PASS: final run `BUILD SUCCESSFUL in 2m 40s` |
| Unit tests | PASS: 7 tests, 0 failures/errors/skips (6 Explore tests plus 1 existing example) |
| Emulator fixture tests | PASS: loading/error/empty/retry, combined filters/reset/clear, null fields/invalid identity, saved-state restoration, ViewModel retry and no refetch on search |
| Live emulator integration | PASS: API-backed India case-insensitive search, Europe mismatch, Asia match, `country_details/IND`, actual India details, Back retains controls |
| Instrumentation totals | PASS: 7 tests, 0 failures/errors/skips on Travel_Phase1_API37, Android 17 / API 37.1 |
| Glide visual smoke check | PASS: live Explore list inspected with loaded PNG flags; screenshots captured during the original verification |
| Final lint | PASS: 0 errors, 35 existing warnings and 1 hint |
| Baseline lint on clean `origin/main` at `200513d` | Originally failed on `NonObservableLocale` in CityScreen; the small authorized fix resolves it |
| Git diff/secret review | Whitespace check passed; installed key absent from tracked/untracked non-ignored source; `local.properties` remains ignored and untracked |

The final command above verifies the source including the CityScreen locale fix. The earlier initial build overlapped source edits and is not a clean baseline claim. Earlier failed lint logs are retained separately from the successful final run.

Evidence is retained outside Git at `/home/ravitejas/projects/Travel-Explorer-Phase1-Evidence/`: build logs, JUnit XML, connected-device XML, lint reports, baseline lint log, safe API status/count metadata, and live test log. Screenshots are under `docs/screenshots/`.

Unverified: physical-device behavior/demo; alternate display sizes/font scales; downstream states/cities/attractions/map flows; complete worldwide country coverage beyond the repository's first pages; real offline failure classification (the existing repository suppresses region errors). Fixture tests validate the Explore error/empty UI, not a corrected repository failure policy.

The local machine has SDK 37.0, emulator image 37.1, Gradle wrapper 9.8.2, and the project's Java 25 toolchain downloaded by Gradle. No dependency or toolchain configuration was changed. An isolated `Travel_Phase1_API37` AVD was created using the installed image; a physical device was not connected.

The development key was installed by the user directly in ignored `local.properties`; its value is never included in source, reports or Git output. Rotation status is unverified. App-like direct API probes returned HTTP 200 for all five regions and Alpha-3 IND. Default Python User-Agent probes returned 403; that result is not evidence that the app credential is invalid.

## Existing API-owned limitations (Varun)

1. **Pagination is ignored.** `CountryRepository.getAllCountries()` calls each region once, and `CountryData` models only `objects`. A live Asia response reported `total=52`, `count=25`, `limit=25`, `offset=0`, `more=true`. Each of the five region requests returned 25 records. Explore filters only what the repository returns, so countries beyond page one cannot be found. This is not fixed by adding a UI filter. Do not claim the complete worldwide dataset is verified.
2. **Region failures are suppressed.** Each region exception becomes an empty list; the outer result can be successful with an empty or partial dataset. Explore can retry an empty dataset, but cannot detect a hidden partial failure. Error propagation/pagination belong to the API repository.
3. **Details fallback can mask errors.** `getCountryByCode()` uses `getAllCountries().getOrNull()` and may turn a failed fallback into `Result.success(null)`.

These are reported here for owner coordination; no GitHub issue or teammate message was sent and no API-owned code was rewritten.

## Authorized locale fix

The user authorized the small existing CityScreen lint correction needed to complete publication. `CityItemCard` now formats population using `LocalConfiguration.current.locales[0]` (with a `Locale.ROOT` fallback). Compose observes this configuration, so locale changes can update the displayed number. The fix uses the project's existing Compose APIs and adds no dependency. All final checks pass with this change.

## Viva notes

- A composable describes the current UI. `HomeScreen` collects ViewModel state; `HomeContent` renders it. Recomposition redraws the affected UI when state changes.
- The ViewModel owns loading work in `viewModelScope`, so routine screen recomposition does not refetch data. The repository already dispatches network work to IO.
- `filterCountries` is a pure collection operation. AND combines name/region predicates; All skips only the region restriction. Filtering the fetched list is fast and makes no additional API calls.
- `rememberSaveable` preserves query/selected region through recreation; it is not durable app preference storage.
- LazyColumn composes visible rows rather than an entire scrolling list at once. Each card uses a shared component and Glide handles remote PNG loading.
- Alpha-3 is identity, while display names can change or repeat. Only a validated code crosses the existing navigation route; details still fetches its own model.
- Retrofit/Gson and the v5 model belong to Varun; this module consumes their contract. Room/wishlist/trips belong to Gowtham. The official whole-app rubric is not fully satisfied by this feature alone.

See `phase1-requirements.md` for the original evidence-to-code matrix.

## Screenshots

- [Live country list with Glide flags](screenshots/explore-live-list.png)
- [Live search and region selection](screenshots/explore-live-filtered.png)
- [No matches and reset action](screenshots/explore-live-empty.png)
- [India details reached through Alpha-3](screenshots/details-live-ind.png)

## Git and publication status

Verified GitHub account: `R4vitejas`; Git author: `Ravitejas` with the GitHub no-reply email for `R4vitejas`. Repository cloned to `/home/ravitejas/projects/Travel-Destination-Explorer` with no pre-existing local work. `feature/explore` safely fast-forwarded from `940cb1f` to `200513d` (main), preserving the team's history. The local branch's 18 ahead commits are existing team commits brought in by that fast-forward, not new Ravitejas work.

The user authorized fixing the small CityScreen lint error and completing the commit, push and PR workflow. The locale correction is a separate focused commit from the Explore feature. Publication targets `feature/explore` into `main` for teammate review; merging remains a separate team action. No GitHub issues or teammate messages were sent. Phase 2 was not started.
