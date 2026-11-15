# Physical-phone demonstration

The app's implementation is merged. Emulator tests and recordings do not replace the required physical-device demonstration. Ravitejas has a USB-debugging-enabled phone and will perform this check in Android Studio.

## Build and run

1. Download the latest `main` ZIP from GitHub and extract it, or update your local checkout. Open the folder containing `settings.gradle.kts` in Android Studio.
2. Follow the README setup, including the Android SDK and the privately supplied API credential in ignored `local.properties`. A ZIP does not include that local file. Never share the key in screenshots, reports or GitHub.
3. Connect the unlocked phone with USB debugging enabled, accept its debugging prompt, choose it in Android Studio and Run the app.
4. Record the device model, Android version, tested commit (or ZIP download time) and results below. Use the submitted revision.

## Acceptance checklist

- [ ] Explore loads actual countries; name search and region filtering work together.
- [ ] A featured country shows a destination photo, facts and attribution. Research opens a browser; destination sharing opens a real target chooser. Cancel without sending if preferred.
- [ ] Save India, open Wishlist, reopen Details and start a trip with India prefilled.
- [ ] Create a dated trip with notes; reject end-before-start dates; edit it; cancel another edit; confirm deletion only for a disposable trip.
- [ ] Keep one sample trip and saved country, force-stop/relaunch and confirm both remain. The preferred Explore region should also remain.
- [ ] Turn off connectivity temporarily: saved Wishlist/Trips remain usable; restore connectivity afterward. Remote geography/images/maps need a network.
- [ ] India -> Karnataka -> Bengaluru -> tourist attractions -> map; try a different radius and matching search. Open a marker when data/tiles are available.
- [ ] Trip sharing opens the chooser; browser and sharing return safely to the app.
- [ ] Inspect keyboard access, scrolling and larger font/dark-theme usability on the actual phone. Report a reproducible defect rather than marking a failed check complete.

## Result record - fill after running

- Device model:
- Android version:
- Submitted revision / ZIP download time:
- Date and tester:
- Checks passed:
- Failures or limitations:

Do not mark this record complete before performing the demonstration. The repository report/video currently documents emulator evidence; the individual viva remains each member's responsibility.
