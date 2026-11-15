# Phase 1 requirement-to-code matrix (before edits)

Sources read in full: all ten handoff files, including rendered original PDF (one page), original assignment image, full syllabus and full team plan. Source pack remains outside Git.

| Requirement and exact evidence | Owner / implementation target | Existing source at main 200513d |
| --- | --- | --- |
| Syllabus Unit 2, line 15: "Using LazyColumn, LazyRow and Card", "Using Scaffold, Compose Navigation"; plan lines 75-80 assigns Explore to Ravitejas | Ravitejas: HomeScreen, CountryCard, AppNavGraph integration | ExploreDynamicScreen and CountryCardItem embedded in AppNavGraph.kt; extract rather than duplicate |
| Plan lines 34-39: "Search by country name", "Filter by region", "Handle loading, empty, and error states" | Ravitejas: name-only case-insensitive search, five regions plus All, combined predicate, distinct empty/error UI | Existing filter matches name OR region; no region control; one generic empty state |
| Syllabus Unit 4 line 27: "using Retrofit", "using Glide"; PDF p1 item 5 a/b requires both | Ravitejas uses existing repository and Glide card; Varun owns API | CountryRepository.getAllCountries(): Result<List<Country>>; Glide Compose already declared |
| Plan lines 214-216: "Do not use list position or display name as a unique ID"; lines 242-243: pass cca3 | Ravitejas passes actual codes.alpha3 to existing details route | Country.kt uses names.common, codes.alpha3, flag.urlPng, region (v5, not old cca3 field); route country_details/{countryCode} |
| Plan lines 247-254: shared Material3 theme, reusable composables, coroutine-compatible APIs | Ravitejas: ui/home, ui/components; retain MainActivity/theme | TravelExplorerTheme wraps AppNavGraph; existing screen runs coroutine fetch from composition |
| PDF p1 item 5 c/d: "Room Database", "Shared Preference"; syllabus Unit 3 lines 21-23 | Gowtham owns wishlist/trips/Room; SharedPreferences requires later team agreement; excluded from Phase 1 | Not part of Explore implementation; no new persistence/dependencies authorized |
| PDF p1 item 5 e/f: "Resources (at least two)", "minimum two implicit intents" | Project-level requirements; Explore can use string resources, no new intent features in Phase 1 | Existing res/values files; this task must not claim full rubric satisfaction |
| Original assignment image row 5: "Fetch destinations, destination photos, details, save wishlist, CRUD for trips" | Explore listing is Ravitejas; details/API Varun; wishlist/trips Gowtham; photos deferred per explicit Phase 1 scope | Preserve merged details/states/cities/tourist/map modules; no photo API |
| PDF p1 items 2-5: GitHub source, equal contribution checked by commits, physical Android demo, individual viva | Verified R4vitejas GitHub auth and Git author; publish feature/explore only after checks, no merge; document viva | Branch was 0 ahead/18 behind main; fast-forwarded cleanly. No attached device or AVD at inspection |
| Prompt E/F: verify app; blocking failure means leave local | Local build/test/UI evidence, label fixtures vs live; no success PR if blocked | API_KEY absent; repository explicitly rejects blank key; ask privately for rotated development key |

Contract conflict resolved: team plan v3.1 examples are stale relative to live v5 model/service. Keep current contracts. REST Countries official homepage inspected on 2026-11-13 confirms v5 and names/codes/flag fields (https://restcountries.com/). No endpoint migration.

Known API-owned defects from source inspection: getAllCountries catches each region exception and returns success even if all fail, or a partial list if some fail; getCountryByCode can suppress lookup/fallback failures into success(null). Report to Varun, do not rewrite.

Follow-up authorization: the user explicitly authorized the small existing CityScreen locale-formatting lint fix so Phase 1 checks and publication can complete. This does not expand into Phase 2 or API changes.
