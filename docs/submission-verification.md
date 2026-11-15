# Submission verification - 16 November 2026

## Revision and checks

Production source: main merge `223f875`, including Varun's `567c587`, `0c4a27f` and `b1b8b24`, Gowtham's singleton fix and the integrated feature histories. Release changes correct only the city-card instrumentation test and add documentation/evidence.

```bash
bash ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest --console=plain
```

Final run: **BUILD SUCCESSFUL in 8m 11s**. Earlier interrupted cold-build attempts are not counted as passes. Test counts are taken from the final XML results; lint severities from the final XML report.

| Check | Final result |
| --- | --- |
| Debug build | Passed |
| Unit tests | 26 passed; zero failures/errors/skips |
| Emulator tests | 19 passed; zero failures/errors/skips |
| Lint | Zero errors; 55 warnings and one hint |
| Emulator | Travel_Phase1_API37, Android 17 / API 37 |
| Physical device | Pending Ravitejas's phone demonstration; not claimed |

Coverage includes country filtering/pagination/failures/cancellation, PlaceDB field mapping, coordinate validation, attraction category/identity handling, ViewModel cancellation, live core navigation, Room close/reopen CRUD and uniqueness, saved-screen offline editing, preference recreation/reset, awaited/retried saves and external-intent payload/failure handling. City cards now mount once per test and assert disabled semantics for missing/invalid coordinates.

## Live dependencies and recording

PlaceDB responded with 37 Indian regions and 2,672 Karnataka cities; Bengaluru returned latitude 12.97912, longitude 77.5913. These direct endpoint probes are distinct from app-screen observations. The actual app then loaded Karnataka cities and Bengaluru attractions at 5 km, displayed real map tiles/markers and opened Ambedkar Statue details. The changed 3 km request showed a service-error/retry state; returning to the cached 5 km results worked. A direct Overpass primary probe returned HTTP 406 and its mirror timed out during preparation; public-service availability is a limitation, not a successful attraction pass.

The core walkthrough created a real Room trip, edited its title, force-stopped/relaunched the app and confirmed the edited record remained. It then opened/cancelled sharing and confirmed deletion of that disposable trip. India was saved through Details and opened from Wishlist with destination prefill.

The final emulator recording and screenshots document the displayed app behavior. See the recording notes in `docs/video/README.md` for actual coverage and any remote-service limits. The share chooser is opened/cancelled without sending a message. Chrome first-run was completed without signing in; Research destination then displayed the intended India travel information search page. No share message was sent.

## Evidence and final acceptance

- [Project report](project-report.pdf)
- [Submission recording](video/submission-demo.mp4) and [recording notes](video/README.md)
- [Final screenshots](screenshots/submission/)
- [Phone demonstration checklist/result](phone-demonstration.md)
- [Photo sources/licenses](photo-credits.md)

Raw build logs, XML-derived summary, endpoint probes and raw clips are retained locally under `/home/ravitejas/projects/Travel-Explorer-Submission-Evidence/`. Credentials and the locally configured APK are not published. The repository ZIP needs private local API configuration recreated before live country requests can work.

The final physical-device run, institution-specific team details and individual viva remain human acceptance work in #8. A reproducible device defect still needs correction before acceptance.
