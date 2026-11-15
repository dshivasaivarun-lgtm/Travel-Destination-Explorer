# Optional finer trip destinations — estimate

Assessment against main at `223f875`, 16 November 2026. This suggestion has not been implemented. UI redesign is excluded.

The current TripEntity stores only country code/name, title, dates and notes. Existing PlaceDB screens already fetch states and cities, and Overpass supplies attractions with type/ID and coordinates. That makes finer destinations feasible without another service, but selecting a place must be connected to the trip form and persisted correctly.

| Option | Difficulty | Estimated person-hours including focused checks |
| --- | --- | --- |
| Add an optional city/place text field under the selected country | Low | 2–4 |
| Real country → state → city selection using existing services | Moderate | 4–8 |
| Add tourist-place selection from that city as well | Moderate–high | 8–16 total for the full structured hierarchy |

The text-field option does not implement a real location picker. The structured options need country/parent context carried through navigation, loading/empty/error handling, clearing dependent selections when the country changes, and snapshots that can render offline. The Room database is currently version 1; adding destination columns needs a non-destructive migration and tests that preserve existing trips. Attractions should retain OSM type plus ID, because numeric IDs alone are not globally unique.

Existing country-only trips must continue to open/edit after the change. A selected city/attraction should remain optional, and multiple activities/day-by-day itineraries are separate enhancements, not part of these estimates. Estimates are not elapsed-time promises and do not include third-party service outages.

Recommendation: submit the verified MVP first. If the team later selects one extension, country/state/city selection is the smallest structured version; add attractions afterward. No additional feature is needed to complete the current mandatory release.
