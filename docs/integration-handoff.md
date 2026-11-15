# Mandatory integration handoff

The integration branch combines PR #2 (Ravitejas Explore) and PR #3 (Gowtham Room/Wishlist/Trips) without squashing or replacing their author history. Varun's API/details/maps module remains the foundation from main. Existing PRs are not automatically merged or closed.

## Implementation status

The mandatory coding work is already implemented in the integration branch and its review PR. Issues #4 and #7 track acceptance/merge of that implementation, not a second round of feature building. Gowtham's Room/Wishlist/Trips foundation came from PR #3; Varun's API/map foundation came from main. Integration added cross-module navigation, remaining required features, focused corrections and tests while retaining that history.

Issues #5 and #6 are owner verification and fixes for any defects found. Issue #8 covers the shared physical-device demonstration, complete submission video/report and viva. Once review, necessary fixes, merge and final delivery checks pass, the mandatory MVP is complete. Further features are a separate optional backlog.

## Ownership and effort estimates

These are review/verification estimates for human teammates, not promised availability. The integration PR prepares the code so owners can review one concrete result rather than implement the same changes independently.

| Task | Owner | Estimated remaining review/verification effort | Tracking |
| --- | --- | --- | --- |
| Integration and shared navigation | Ravitejas | 4–7 person-hours including implementation/testing | #4 |
| Room/trip correctness and restart persistence | Gowtham | 2–4 hours | #5 |
| Country paging, geography, downstream flow | Varun | 2–4 hours | #6 |
| Preferences, intents, photos and resource coverage | Ravitejas; Varun reviews photo/details integration | 4–7 person-hours including implementation/testing | #7 |
| Physical device, video/PDF and viva preparation | All; Ravitejas assembles | 5–8 combined team-hours | #8 |

Submission target: Monday 16 November 2026. Individual viva: Tuesday 17 November 2026. Optional attraction-to-trip links, live photo APIs, introductions, cloud sync and AI are deferred.

## Changes prepared for owner review

- One Explore implementation, preserving combined country-name/region filtering and tested loading/error states.
- Room-backed wishlist and trip screens connected through a shared navigation graph. Details can save/plan; Wishlist cards open Details and can prefill trip creation.
- Trip editing loads the existing record before showing the form. Writes finish before navigation; duplicate Save taps are disabled and errors remain visible. Forms use a country selector, strict dates/date pickers, scroll/keyboard support and saved input state. Trip deletion requires confirmation.
- SharedPreferences remembers the Explore region; search stays temporary.
- Existing browser/share intents are reused with missing-handler protection. Browser action is accurately labeled Research destination; it opens search, not a verified official website.
- Twelve curated, remotely fetched destination photos with credit/source links. All other countries retain API facts and flags; no worldwide photo coverage is claimed.
- Country region paging is consumed completely; failures are no longer silently flattened into partial success. Cancellation propagates; details failures stay failures.
- Geography uses supplied Alpha-2 values and valid coordinates. No fabricated Bengaluru location; zero coordinates are valid. Map receives the selected radius, and obsolete radius requests are cancelled.

## Release checks still owned by the team

- Inspect the integration PR before deciding how to merge it alongside the existing feature PRs. It contains both feature histories; do not reimplement those modules.
- Gowtham: verify duplicate wishlist actions, trip CRUD and force-stop/relaunch persistence in the combined app; inspect saved data and cancellation/error behavior.
- Varun: search for a country previously beyond page one; verify real network failures and the states/cities/attractions/map flow, radius consistency and image credits.
- All: physical Android device demonstration, browser/share target apps, small screen/large fonts, connectivity failures, final screenshots, video/PDF and individual viva.
- No physical device was connected during initial integration. Emulator and database-reopen evidence must not be described as a physical-device pass.

## Demo and viva script

1. Show API-backed Explore, country-name search, combined region filtering and reset.
2. Open a featured country: photo, flag, facts, image attribution and Research destination.
3. Save country; open Wishlist; reopen Details; Plan trip with destination prefilled.
4. Create a trip with dates/notes; show listing, edit, cancellation and confirmed deletion.
5. Demonstrate Share destination and Share trip using Android's chooser; cancel safely.
6. Change Explore region, force-stop/relaunch, show restored preference and persistent saved records.
7. Show existing states → cities → attractions → map and radius matching after Varun verifies them.
8. Offline: saved Wishlist/Trips remain accessible, remote failures offer retry, photos can show placeholders.

Explain Compose state vs ViewModel state vs SharedPreferences vs Room; Retrofit/Gson paging/error handling; Glide; implicit intents; Android resources; canonical Alpha-3 vs supplied Alpha-2; and exactly what each member contributed. Never present a fixture test as a real API/device pass.
