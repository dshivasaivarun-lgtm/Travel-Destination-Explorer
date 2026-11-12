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
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.Locale

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

    val places = (uiState as? TravelMapUiState.Success)?.places ?: emptyList()

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
            OsmMapContainer(
                cityLat = latitude,
                cityLon = longitude,
                touristPlaces = places,
                onPlaceSelected = { place -> selectedPlace = place }
            )

            when (val state = uiState) {
                is TravelMapUiState.Loading -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("Loading attractions...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is TravelMapUiState.Error -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer,
                        tonalElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Attractions unavailable",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Button(
                                onClick = { viewModel.loadMapData(latitude, longitude) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {}
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
fun OsmMapContainer(
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
                Lifecycle.Event.ON_RESUME -> mv.onResume()
                Lifecycle.Event.ON_PAUSE -> mv.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }

    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().userAgentValue = "TravelExplorer-AndroidApp/1.0"
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(cityLat, cityLon))
                mapViewRef = this
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mv ->
            mv.controller.setCenter(GeoPoint(cityLat, cityLon))
            mv.overlays.clear()

            touristPlaces.filter { it.latitude != 0.0 && it.longitude != 0.0 }.forEach { place ->
                val marker = Marker(mv).apply {
                    position = GeoPoint(place.latitude, place.longitude)
                    title = place.name
                    snippet = place.category
                    setOnMarkerClickListener { _, _ ->
                        onPlaceSelected(place)
                        true
                    }
                }
                mv.overlays.add(marker)
            }
            mv.invalidate()
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
