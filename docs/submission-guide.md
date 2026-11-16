# Submission and demonstration guide

## Repository deliverables

- Source and setup: repository root and README.
- Submitted report: `docs/Travel_Destination_Explorer_Report.pdf` (the exact PDF submitted by the team).
- Recording: `docs/video/submission-demo.mp4` (actual emulator interaction; idle waits trimmed).
- Latest verification: `docs/submission-verification.md`.
- Photo credits: `docs/photo-credits.md`.
- Physical-phone checklist/result: `docs/phone-demonstration.md`.

The earlier `project-report.pdf` and `integration-review.pdf` are supporting development evidence. The old 21-second integration preview was removed during repository cleanup. Use the submitted report and complete recording above for evaluation.

The team has confirmed that the submitted report and repository link were sent. The submitted PDF is preserved unchanged.

## Final demonstration and viva checks

1. Download/update the final main revision and run it on the physical phone. Complete the device record; keep unresolved defects visible until corrected.
2. Open the PDF and video from GitHub to ensure they play/download. Check any institution-specific cover details/student identifiers against the team's actual records; no identifiers are invented here.
3. Keep the API key private. A source ZIP needs the ignored local configuration recreated before live API requests can work.
4. Each teammate prepares for the individual viva. Show the same final revision that was tested.

## Viva prompts

- Ravitejas: combined search/region predicate, navigation and prefill, state versus persistence, resource and external-intent usage, integration/testing decisions.
- Varun: Retrofit/Gson, paging and cancellation, Alpha-2 versus Alpha-3, PlaceDB coordinate fields, Overpass categories and OSM type/ID, radius/query matching on the map.
- Gowtham: Room entities/DAOs, singleton initialization, wishlist uniqueness, trip CRUD, strict date validation, database reopen persistence and safe writes.
- Everyone: explain the complete Explore -> Details -> Wishlist -> Trip journey, distinguish offline saved text from online resources, and explain their own actual commits.

Mandatory implementation is complete. Optional finer destinations, expanded photography and day-by-day itineraries can be considered after submission; they are not hidden release requirements. Physical demonstration and individual viva remain human work.
