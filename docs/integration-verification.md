# Integration verification (historical)

**16 November update:** PR #10 and Varun's fixes are merged into main. Final checks pass with 26 unit and 19 emulator tests; see [submission verification](submission-verification.md) and [release status](release-status.md). The dated entries below describe earlier revisions, not the current release status.

## Latest owner acceptance — 15 November 2026

Source commit: `79f14fb`. Gowtham's Room singleton initialization correction (`c29c97a`) is merged with original authorship preserved.

Full command: `bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest --console=plain` — **BUILD SUCCESSFUL in 1m 38s**.

- **15 unit tests and 17 emulator tests passed**, zero failures/errors/skips.
- Lint: zero errors, 55 warnings and 1 hint remain.
- New offline acceptance disables Wi-Fi/mobile data only on the isolated emulator, opens saved Wishlist/Trips, edits a saved trip successfully and restores connectivity/test records. It does not switch connectivity on a physical phone.
- Four external-action checks verify the web action and plain-text chooser payload, missing-handler safety and invalid-link rejection. These use a capturing Context; they do not send a message or claim a physical target-app pass.
- Live journey test now waits for Details data after Wishlist navigation and scrolls validation text into view. Earlier attempts exposed these test timing/viewport assumptions; the final full suite passed after correcting them.
- Raw logs/XML/summary: `/home/ravitejas/projects/Travel-Explorer-Integration-Evidence/owner-acceptance/`.

Gowtham has [reported completed emulator validation and code review](https://github.com/dshivasaivarun-lgtm/Travel-Destination-Explorer/issues/5#issuecomment-6880454347). Your #4/#7 implementation/emulator acceptance and his #5 verification scope are complete. Varun's #6 verification remains open. Merge, physical-device checks and final submission are tracked together in #8. No main-branch merge was performed.

See [current release status and next steps](release-status.md). The existing PDF/preview below are the earlier review artifacts, not the final submission package.

## Earlier integration evidence — 14 November 2026

Source: `integration/mandatory-features`, combining PR #2 at `27e612b` and PR #3 at `8bbeef0` with focused integration/correctness changes. Remote heads were checked again before publication and remained unchanged. No existing PR was merged on GitHub.

## Automated checks

Command:

```bash
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest --console=plain
```

Final run: **BUILD SUCCESSFUL in 1m 26s**. Android emulator: Travel_Phase1_API37, Android 17/API 37.1.

| Check | Result |
| --- | --- |
| Unit tests | 15 passed; zero failures/errors/skips |
| Connected tests | 12 passed; zero failures/errors/skips |
| Lint | Zero errors; 55 warnings and one hint remain |
| Photo URLs | All 12 returned HTTP 200; photo/caption contact sheet visually checked |
| Whitespace | `git diff --check` passed |
| Credential review | Configured credential absent from non-ignored files; `local.properties` ignored |

Unit coverage: six existing Explore predicate/identity tests, five country pagination/failure/cancellation tests, three date/geography tests and one existing example. Connected coverage: seven prior Explore/example tests, a live core navigation/prefill/cancel test, two persistence/preference tests, and two awaited-save/failure/retry tests. The database persistence test uses a separate on-device file, closes/reopens it, verifies wishlist uniqueness and trip CRUD, then removes its test file.

The first new journey-test run had an ambiguous text selector matching the search field and India card. The selector was corrected; the final complete run above passed. Earlier diagnostic build/test failures are not presented as final passes.

Raw logs, XML, lint and summary are retained outside Git at `/home/ravitejas/projects/Travel-Explorer-Integration-Evidence/`. Credentials and machine-local settings are not in the repository.

## Manual emulator observations

- API-backed Explore list, name search, destination photo and credit/source display inspected.
- Details saves a country; Wishlist opens Details and prefills trip destination.
- Created a dated trip using date pickers; it survived app force-stop/relaunch and was visible in Trips.
- Edited the trip, opened the Android share chooser with correct destination/dates, cancelled sharing, and used confirmed deletion.
- Research destination launched Chrome’s first-run activity. Browser onboarding and the final research page were not completed.
- Selected Explore region restored after force-stop/relaunch.
- Screenshot evidence is under `docs/screenshots/integration/`.
- The initial emulator session exited while recording; that incomplete video was discarded. A subsequent recording was finalized and checked. The preview is intentionally limited to trip creation/relaunch and is **not** the final all-functionality submission video.

## Artifacts and remaining release work

- [Integration review PDF](integration-review.pdf): team/contributions, requirement coverage, verification, core screenshots and photo credits; rendered and visually checked. This is a review report, not the final institution-formatted submission.
- [Short emulator preview](video/integration-preview.mp4): actual emulator recording with idle portions trimmed; no audio. Final complete-functionality recording remains issue #8.
- [Ownership/release/viva handoff](integration-handoff.md)
- [Photo credits](photo-credits.md)

No physical Android device was connected. Physical-device behavior, small-screen/large-font/dark-theme checks, actual share-target completion, unavailable-handler behavior on a device without apps, and downstream PlaceDB/Overpass/map runtime flows remain owner verification tasks. The source changes and unit checks do not constitute a complete downstream live pass.

Gowtham owns issue #5; Varun owns issue #6; shared final submission/device/viva work is issue #8. New optional features remain deferred.
