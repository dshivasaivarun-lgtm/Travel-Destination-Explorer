# Travel Destination Explorer

Android travel exploration and local trip planning using Kotlin, Jetpack Compose/Material 3, Retrofit/Gson, Glide, Room and SharedPreferences.

## Features

- API-backed country list, case-insensitive name search and combined region filtering.
- Country facts/flags, 12 curated featured destination photos with attribution, research/share actions.
- Persistent wishlist and trip create/read/edit/delete, country selection, date pickers and validation.
- Explore/Wishlist/Trips navigation and persistent preferred region.
- Existing states, cities, tourist attractions and interactive OpenStreetMap exploration.

Photo coverage is limited to the featured catalogue. Saved text remains available offline; country browsing, remote images, PlaceDB, Overpass and map tiles need connectivity. Glide's cache is not a guaranteed offline image library.

## Local setup

1. Open the repository in Android Studio and use the checked-in Gradle wrapper/toolchain settings. The project compiles against Android SDK 37, targets 37 and supports Android 7.0/API 24 or later. Gradle provisions the configured JDK; do not independently change dependency versions across branches.
2. Install the SDK requested by Android Studio. Set `sdk.dir` in ignored `local.properties` if needed.
3. Obtain a current development credential privately from the API owner and add `API_KEY=...` to ignored `local.properties`. Never commit or paste the credential into reports/issues. BuildConfig avoids committing the key but does not conceal it in an APK.
4. Build and run on an emulator or USB-debugging-enabled physical device. An empty key can build, but live country requests cannot succeed.

```bash
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
bash ./gradlew :app:connectedDebugAndroidTest
```

Connected live tests require a working key and network. A test skipped for missing credentials is not a live pass. Tests create/delete their own records or restore the saved preference; the persistence regression uses a separate on-device database.

## Team and submission

- Ravitejas: Explore/Home, integration, shared UI and regression checks.
- Dharoor Shiva Sai Varun: API/details and geographic/map exploration.
- Gowtham B: Room, Wishlist and Trips foundation.

[Integration verification and artifacts](docs/integration-verification.md) · [Review PDF](docs/integration-review.pdf) · [Emulator preview](docs/video/integration-preview.mp4) · [Integration ownership, release checklist and viva script](docs/integration-handoff.md) · [Photo sources and licenses](docs/photo-credits.md) · [Phase 1 verification](docs/phase1-explore.md)

The integration branch contains both feature PR histories for review. Existing feature PRs are not automatically merged. Physical-device verification and final video/PDF submission are tracked in issue #8; see the integration verification notes for actual automated results and any emulator artifacts.
