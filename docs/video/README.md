# Submission recording

[Watch/download the complete emulator demonstration](submission-demo.mp4). Duration: **10 minutes 00 seconds**. Android 17 / API 37 emulator; production source main `223f875`. No audio.

Actual app interaction is recorded, with long visually static waits trimmed. No simulated map data or screenshot slideshow replaces the live app. Original raw clips are retained locally.

| Start | Demonstrated behavior |
| --- | --- |
| 00:00 | Explore, search/region filtering, photo/details and Wishlist |
| 00:50 | Trip creation with dates and notes |
| 02:07 | Trip editing, restart persistence, sharing and confirmed deletion |
| 03:16 | Country facts, states and cities |
| 04:15 | Bengaluru attractions and changed radius |
| 04:53 | 3 km map loading/service failure |
| 05:10 | 5 km map, markers and selected attraction details |
| 05:43 | Preference restore, empty search, destination share, offline Wishlist and removal |
| 07:29 | Reversed-date validation and cancellation |
| 08:30 | Research destination in Chrome; first-run onboarding |
| 09:40 | Intended India travel information search page |
| 09:45 | Destination share chooser, followed by cancellation |

## Observed limits

- The 5 km attraction request succeeded; map tiles/markers and Ambedkar Statue details were displayed. A 3 km request failed and showed the retry state. The recording then returns to the successfully loaded 5 km results. Public services remain connectivity-dependent.
- Destination and trip share choosers were opened and cancelled; no message was sent.
- Chrome first-run was completed without signing in. Research destination then displayed the intended search page. External websites are separate from the app.
- This is emulator evidence. The required physical-phone demonstration remains in [the device checklist](../phone-demonstration.md).

The older 21-second integration preview was removed during repository cleanup. This complete recording is the submission walkthrough.
