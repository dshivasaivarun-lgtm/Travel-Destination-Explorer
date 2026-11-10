package com.pes.travelexplorer.ui.cities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pes.travelexplorer.data.model.City
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    countryCode: String,
    admin1Id: String,
    stateName: String,
    viewModel: CityViewModel = viewModel(),
    onTouristPlacesClick: (String, Double, Double) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(countryCode, admin1Id) {
        viewModel.loadCities(countryCode, admin1Id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cities in $stateName") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search cities...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is CityUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CityUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadCities(countryCode, admin1Id) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is CityUiState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No cities found for this administrative division.")
                    }
                }
                is CityUiState.Success -> {
                    val filteredCities = state.cities.filter {
                        it.name?.contains(searchQuery, ignoreCase = true) == true
                    }

                    if (filteredCities.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No matching cities found.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredCities) { city ->
                                CityItemCard(city = city, onTouristPlacesClick = onTouristPlacesClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CityItemCard(city: City, onTouristPlacesClick: (String, Double, Double) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = city.name ?: "Unknown City",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (city.population != null) {
                val formattedPop = NumberFormat.getNumberInstance(Locale.getDefault()).format(city.population)
                Text(
                    text = "Population: $formattedPop",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (city.latitude != null && city.longitude != null) {
                Text(
                    text = "Coordinates: ${String.format(Locale.US, "%.2f", city.latitude)}, ${String.format(Locale.US, "%.2f", city.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Button(
                onClick = {
                    val lat = city.latitude ?: 12.9716
                    val lon = city.longitude ?: 77.5946
                    onTouristPlacesClick(city.name ?: "City", lat, lon)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Tourist Attractions")
            }
        }
    }
}
