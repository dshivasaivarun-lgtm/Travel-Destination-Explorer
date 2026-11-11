package com.pes.travelexplorer.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pes.travelexplorer.data.model.TouristPlace
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import java.util.Locale

private const val MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelMapScreen(
    cityName: String,
    latitude: Double,
    longitude: Double,
    viewModel: TravelMapViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPlace by remember { mutableStateOf<TouristPlace?>(null) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(latitude, longitude) {
        viewModel.loadMapData(latitude, longitude)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Map - $cityName") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "© OpenStreetMap contributors",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TravelMapUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is TravelMapUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadMapData(latitude, longitude) }) {
                            Text("Retry")
                        }
                    }
                }
                is TravelMapUiState.Success -> {
                    MapLibreContainer(
                        cityLat = latitude,
                        cityLon = longitude,
                        touristPlaces = state.places,
                        onPlaceSelected = { place -> selectedPlace = place }
                    )
                }
            }

            selectedPlace?.let { place ->
                ModalBottomSheet(
                    onDismissRequest = { selectedPlace = null },
                    sheetState = sheetState
                ) {
                    PlaceDetailBottomSheetContent(place = place)
                }
            }
        }
    }
}

@Composable
fun MapLibreContainer(
    cityLat: Double,
    cityLon: Double,
    touristPlaces: List<TouristPlace>,
    onPlaceSelected: (TouristPlace) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val mv = mapViewRef ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_START -> mv.onStart()
                Lifecycle.Event.ON_RESUME -> mv.onResume()
                Lifecycle.Event.ON_PAUSE -> mv.onPause()
                Lifecycle.Event.ON_STOP -> mv.onStop()
                Lifecycle.Event.ON_DESTROY -> mv.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = { ctx ->
            MapLibre.getInstance(ctx)
            MapView(ctx).apply {
                onCreate(null)
                onStart()
                onResume()
                mapViewRef = this
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mv ->
            mv.getMapAsync { map ->
                map.setStyle(Style.Builder().fromUri(MAP_STYLE_URL)) { _ ->
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(cityLat, cityLon))
                        .zoom(12.0)
                        .build()

                    map.clear()
                    val markerToPlaceMap = mutableMapOf<Long, TouristPlace>()

                    touristPlaces.filter { it.latitude != 0.0 && it.longitude != 0.0 }.forEach { place ->
                        val options = MarkerOptions()
                            .position(LatLng(place.latitude, place.longitude))
                            .title(place.name)
                            .snippet(place.category)

                        val marker: Marker = map.addMarker(options)
                        markerToPlaceMap[marker.id] = place
                    }

                    map.setOnMarkerClickListener { clickedMarker ->
                        val place = markerToPlaceMap[clickedMarker.id]
                        if (place != null) {
                            onPlaceSelected(place)
                            true
                        } else false
                    }
                }
            }
        }
    )
}

@Composable
fun PlaceDetailBottomSheetContent(place: TouristPlace) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = place.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        if (!place.address.isNullOrBlank()) {
            Text(text = "📍 ${place.address}", style = MaterialTheme.typography.bodyMedium)
        }

        if (!place.openingHours.isNullOrBlank()) {
            Text(text = "🕒 Hours: ${place.openingHours}", style = MaterialTheme.typography.bodyMedium)
        }

        if (!place.website.isNullOrBlank()) {
            Text(text = "🌐 ${place.website}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }

        Text(
            text = "Coordinates: ${String.format(Locale.US, "%.4f", place.latitude)}, ${String.format(Locale.US, "%.4f", place.longitude)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
