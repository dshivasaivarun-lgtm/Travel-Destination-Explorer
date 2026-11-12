# Travel Destination Explorer — Complete Module Technical Documentation

**Project Name:** Travel Destination Explorer  
**Package:** `com.pes.travelexplorer`  
**Git Branch:** `feature/api-details`  
**Developer:** Varun  
**Architecture:** Modern Android Architecture (Jetpack Compose + MVVM + Clean Repository Pattern + Coroutines + StateFlow)  
**Remote Repository:** [GitHub - Travel Destination Explorer](https://github.com/dshivasaivarun-lgtm/Travel-Destination-Explorer.git)  

---

## 1. Overview of Exploration Flow (6 Levels)

The module establishes a complete, hierarchical 6-level travel exploration flow:

```text
[Level 1: Explore All Countries]
            │
            ▼
[Level 2: Country Details] ──(View States & Provinces)──► [Level 3: States/Provinces]
                                                                     │
                                                           (Tap State Card)
                                                                     │
                                                                     ▼
                                                          [Level 4: Cities/Settlements]
                                                                     │
                                                           (Tap City Card)
                                                                     │
                                                                     ▼
                                                         [Level 5: Tourist Attractions]
                                                                     │
                                                            (Tap Map Icon)
                                                                     │
                                                                     ▼
                                                       [Level 6: Interactive Map (OSM)]
```

1. **Level 1: Explore All Countries (`ExploreDynamicScreen`)**:
   - Queries REST Countries API v5 to list countries worldwide.
   - Displays flag images, common names, and continent regions.
   - Includes real-time local search filtering.
2. **Level 2: Country Details (`CountryDetailScreen`)**:
   - Fetches country details by Alpha-3 code.
   - Displays national flag (Glide), official/common names, Alpha-3 code, capital, region, subregion, population (formatted with commas), currencies, and languages.
   - Features a **"View States & Provinces"** action button.
3. **Level 3: States & Administrative Divisions (`StateScreen`)**:
   - Queries PlaceDB API (`/v1/regions/{countryCode}.json`) using the country's ISO Alpha-2 code.
   - Displays all administrative divisions (states, provinces, prefectures, regions) with local search filtering.
   - Tapping any state card opens its cities.
4. **Level 4: Cities & Settlements (`CityScreen`)**:
   - Queries PlaceDB API (`/v1/cities/{countryCode}/{admin1Id}.json`) using the state's unique ID.
   - Displays cities with population figures and GPS coordinates (lat/lon).
   - **Direct Card Click**: Tapping anywhere on a city card immediately opens nearby tourist attractions.
5. **Level 5: Tourist Attractions & Places (`TouristPlacesScreen`)**:
   - Queries OpenStreetMap Overpass API (`interpreter`) centered on the city's GPS coordinates.
   - Filters tags for `tourism`, `historic`, and `leisure=park` (attractions, museums, viewpoints, parks, historic monuments).
   - Features radius selection chips (3 km, 5 km, 10 km) and real-time search.
   - Displays place name, category badge, address, opening hours, website, coordinates, and ODbL attribution.
6. **Level 6: Interactive OpenStreetMap (`TravelMapScreen`)**:
   - Uses **`osmdroid`** (`org.osmdroid:osmdroid-android:6.1.20`) with standard `TileSourceFactory.MAPNIK` tiles (`© OpenStreetMap contributors`).
   - 100% pure Kotlin/Java map rendering (no native C++ crashes).
   - Map opens centered on city coordinates with full multi-touch zoom and panning.
   - Renders interactive markers for all tourist attractions. Tapping a marker opens a Material 3 **`ModalBottomSheet`** showing details.

---

## 2. APIs Integrated, Base URLs & Authentication

### A. REST Countries API v5
- **Base URL:** `https://api.restcountries.com/countries/v5/`
- **Authentication:** `Authorization: Bearer <API_KEY>`
- **Security Storage:** Read from `local.properties` (`API_KEY=...`), injected via Gradle into `BuildConfig.API_KEY`, and appended by an OkHttp `Interceptor` in `RetrofitClient`.
- **Endpoints Used:**
  - `GET names.common/{name}` (Fetch country by common name)
  - `GET codes.alpha_3/{code}` (Fetch country by 3-letter ISO code)
  - `GET region/{region}` (Fetch countries by continent: Africa, Americas, Asia, Europe, Oceania)

### B. PlaceDB API (States & Cities)
- **Base URL:** `https://api.placedb.org/`
- **Authentication:** None (Free open geographic database)
- **Endpoints Used:**
  - `GET v1/regions/{countryCode}.json` (Fetch states/provinces using ISO Alpha-2 country code)
  - `GET v1/cities/{countryCode}/{admin1Id}.json` (Fetch settlements using state ID)
- **Response Wrapper Handling:** Flexible Gson parsing for `RegionResponse` and `CityResponse` mapping properties (`data`, `regions`, `cities`, `items`, `settlements`).

### C. OpenStreetMap Overpass API (Tourist Attractions)
- **Primary Base URL:** `https://overpass-api.de/api/`
- **Secondary Mirror Base URL:** `https://overpass.kumi.systems/api/`
- **Authentication:** None required, strictly requires custom `User-Agent: TravelExplorer-AndroidApp/1.0 (Student Project)`.
- **Endpoint Used:**
  - `POST interpreter` with `Content-Type: text/plain; charset=utf-8` sending Overpass QL query:
    ```ql
    [out:json][timeout:35];
    (
      node(around:5000,LAT,LON)[tourism];
      node(around:5000,LAT,LON)[historic];
      node(around:5000,LAT,LON)[leisure=park];
      way(around:5000,LAT,LON)[tourism];
      way(around:5000,LAT,LON)[historic];
      way(around:5000,LAT,LON)[leisure=park];
    );
    out center;
    ```
- **Automatic Mirror Failover:** `TouristPlaceRepository` automatically attempts primary server first; if HTTP 502 Bad Gateway, 504 Gateway Timeout, or 429 Rate Limit occurs, it instantly failovers to `mirrorApi` (`overpass.kumi.systems`).
- **Caching:** In-memory key-value caching (`cache["${lat}_${lon}_$radius"]`) to prevent redundant requests and respect ODbL rate guidelines.

### D. OpenStreetMap Tile Server
- **Provider:** `TileSourceFactory.MAPNIK` (`https://tile.openstreetmap.org/{z}/{x}/{y}.png`)
- **Attribution:** `© OpenStreetMap contributors`

---

## 3. Technology Stack & Dependencies

```toml
[versions]
agp = "9.3.3"
coreKtx = "1.19.1"
lifecycleRuntimeKtx = "2.11.0"
activityCompose = "1.13.0"
kotlin = "2.2.10"
composeBom = "2026.02.01"
gson = "2.15.0"
retrofit = "3.0.0"
coroutines = "1.11.0"
glide = "4.16.0"
glideCompose = "1.0.0-beta01"
navigation = "2.8.7"
osmdroid = "6.1.20"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
gson = { group = "com.google.code.gson", name = "gson", version.ref = "gson" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
glide = { group = "com.github.bumptech.glide", name = "glide", version.ref = "glide" }
glide-compose = { group = "com.github.bumptech.glide", name = "compose", version.ref = "glideCompose" }
osmdroid-android = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }
```

---

## 4. Complete Project File Manifest

```text
app/src/main/java/com/pes/travelexplorer/
├── data/
│   ├── model/
│   │   ├── Country.kt                   # REST Countries v5 Gson models (CountryResponse, Country, CountryCodes, etc.)
│   │   ├── AdministrativeDivision.kt    # PlaceDB Region models (RegionResponse, AdministrativeDivision)
│   │   ├── City.kt                      # PlaceDB City models (CityResponse, City)
│   │   └── TouristPlace.kt              # OpenStreetMap Overpass models (OverpassResponse, TouristPlace)
│   ├── remote/
│   │   ├── CountryApi.kt                # Retrofit interface for REST Countries API
│   │   ├── RetrofitClient.kt            # OkHttp Client + Auth Interceptor for REST Countries API
│   │   ├── PlaceDbApi.kt                # Retrofit interface for PlaceDB API
│   │   ├── PlaceDbClient.kt             # OkHttp Client + Logging for PlaceDB API
│   │   ├── OverpassApi.kt               # Retrofit interface for Overpass QL POST
│   │   └── OverpassClient.kt            # OkHttp Client + User-Agent + Primary & Mirror Base URLs
│   └── repository/
│       ├── CountryRepository.kt         # Coroutine repository for country list & alpha-3 lookup
│       ├── StateRepository.kt           # Coroutine repository for ISO Alpha-2 state lookup
│       ├── CityRepository.kt            # Coroutine repository for city settlements lookup
│       └── TouristPlaceRepository.kt    # Coroutine repository for Overpass QL with mirror failover & cache
├── ui/
│   ├── navigation/
│   │   └── AppNavGraph.kt               # Jetpack Compose NavHost (explore, country_details, states, cities, tourist_places, map)
│   ├── details/
│   │   ├── CountryDetailViewModel.kt    # M3 ViewModel for Country details StateFlow
│   │   └── CountryDetailScreen.kt       # M3 Screen for Country details & View States button
│   ├── states/
│   │   ├── StateViewModel.kt            # M3 ViewModel for States StateFlow
│   │   └── StateScreen.kt               # M3 Screen for States list & search
│   ├── cities/
│   │   ├── CityViewModel.kt             # M3 ViewModel for Cities StateFlow
│   │   └── CityScreen.kt               # M3 Screen for Cities list, search & card-click
│   ├── tourist/
│   │   ├── TouristPlacesViewModel.kt    # M3 ViewModel for Tourist Attractions StateFlow
│   │   └── TouristPlacesScreen.kt       # M3 Screen for attractions list, search, radius selector & map action
│   └── map/
│       ├── TravelMapViewModel.kt        # M3 ViewModel for Map StateFlow
│       └── TravelMapScreen.kt           # M3 Screen for osmdroid MapView, markers & BottomSheet
└── MainActivity.kt                       # Entry activity launching TravelExplorerTheme { AppNavGraph() }
```

---

## 5. Navigation Route Contracts & Team Integration

### Navigation Routes Defined in `AppNavGraph.kt`:
1. `explore`
2. `country_details/{countryCode}`
3. `states/{countryCode}`
4. `cities/{countryCode}/{admin1Id}/{stateName}`
5. `tourist_places/{cityName}/{lat}/{lon}`
6. `map/{cityName}/{lat}/{lon}`

### Integration Contracts for Teammates:
- **Ravitejas (Explore / Home Module)**:
  When a user taps a country card on your Explore screen, trigger navigation to:
  ```kotlin
  navController.navigate("country_details/${country.codes?.alpha3}")
  ```
- **Gowtham (Wishlist & Trip Planner Module)**:
  Use `codes.alpha_3` (e.g. `"IND"`, `"JPN"`, `"FRA"`, `"USA"`) as the canonical unique primary key across database entities.

---

## 6. Security, Permissions & Build Verification

- **Permissions (`AndroidManifest.xml`)**:
  `<uses-permission android:name="android.permission.INTERNET" />` declared at root manifest level.
- **Build Status**:
  Gradle build task `app:assembleDebug` passed **successfully with 0 errors**.
- **Runtime Test Results**:
  Validated in Android Emulator (API 29)—all 6 levels of exploration, network requests, state flows, bottom sheets, search filters, mirror failovers, and interactive OSM maps function with zero crashes.
