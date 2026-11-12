package com.pes.travelexplorer.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.repository.CountryRepository
import com.pes.travelexplorer.ui.cities.CityScreen
import com.pes.travelexplorer.ui.details.CountryDetailScreen
import com.pes.travelexplorer.ui.map.TravelMapScreen
import com.pes.travelexplorer.ui.states.StateScreen
import com.pes.travelexplorer.ui.tourist.TouristPlacesScreen
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "explore"
    ) {
        composable("explore") {
            ExploreDynamicScreen(
                onCountryClick = { countryCode ->
                    navController.navigate("country_details/$countryCode")
                }
            )
        }
        composable(
            route = "country_details/{countryCode}",
            arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "IND"
            CountryDetailScreen(
                countryCode = countryCode,
                onBackClick = { navController.popBackStack() },
                onViewStatesClick = { alpha2 ->
                    navController.navigate("states/$alpha2")
                }
            )
        }
        composable(
            route = "states/{countryCode}",
            arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "IN"
            StateScreen(
                countryCode = countryCode,
                onStateClick = { admin1Id, stateName ->
                    val encodedName = URLEncoder.encode(stateName, StandardCharsets.UTF_8.toString())
                    navController.navigate("cities/$countryCode/$admin1Id/$encodedName")
                },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(
            route = "cities/{countryCode}/{admin1Id}/{stateName}",
            arguments = listOf(
                navArgument("countryCode") { type = NavType.StringType },
                navArgument("admin1Id") { type = NavType.StringType },
                navArgument("stateName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "IN"
            val admin1Id = backStackEntry.arguments?.getString("admin1Id") ?: ""
            val encodedName = backStackEntry.arguments?.getString("stateName") ?: "State"
            val stateName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            CityScreen(
                countryCode = countryCode,
                admin1Id = admin1Id,
                stateName = stateName,
                onBackClick = { navController.popBackStack() },
                onTouristPlacesClick = { cityName, lat, lon ->
                    val encodedCity = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString())
                    navController.navigate("tourist_places/$encodedCity/$lat/$lon")
                }
            )
        }
        composable(
            route = "tourist_places/{cityName}/{lat}/{lon}",
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lon") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedCity = backStackEntry.arguments?.getString("cityName") ?: "City"
            val cityName = URLDecoder.decode(encodedCity, StandardCharsets.UTF_8.toString())
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 12.9716
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 77.5946
            TouristPlacesScreen(
                cityName = cityName,
                latitude = lat,
                longitude = lon,
                onBackClick = { navController.popBackStack() },
                onViewOnMapClick = { name, latitude, longitude ->
                    val enc = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                    navController.navigate("map/$enc/$latitude/$longitude")
                }
            )
        }
        composable(
            route = "map/{cityName}/{lat}/{lon}",
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lon") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedCity = backStackEntry.arguments?.getString("cityName") ?: "City"
            val cityName = URLDecoder.decode(encodedCity, StandardCharsets.UTF_8.toString())
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 12.9716
            val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull() ?: 77.5946
            TravelMapScreen(
                cityName = cityName,
                latitude = lat,
                longitude = lon,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ExploreDynamicScreen(
    onCountryClick: (String) -> Unit
) {
    var countries by remember { mutableStateOf<List<Country>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun loadCountries() {
        scope.launch {
            isLoading = true
            errorMessage = null
            val result = CountryRepository().getAllCountries()
            result.fold(
                onSuccess = { list ->
                    countries = list.sortedBy { it.names?.common }
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = error.localizedMessage ?: "Failed to load countries"
                    isLoading = false
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        loadCountries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Explorer - All Countries") },
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
                placeholder = { Text("Search all countries...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { loadCountries() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    val filtered = countries.filter {
                        it.names?.common?.contains(searchQuery, ignoreCase = true) == true ||
                        it.region?.contains(searchQuery, ignoreCase = true) == true
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No countries found.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered) { country ->
                                CountryCardItem(country = country) {
                                    val alpha3 = country.codes?.alpha3
                                    if (!alpha3.isNullOrBlank()) {
                                        onCountryClick(alpha3)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CountryCardItem(country: Country, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val flagUrl = country.flag?.urlPng ?: country.flag?.urlSvg
            if (!flagUrl.isNullOrBlank()) {
                Card(
                    modifier = Modifier.size(56.dp, 36.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    GlideImage(
                        model = flagUrl,
                        contentDescription = "Flag",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.names?.common ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!country.region.isNullOrBlank()) {
                    Text(
                        text = country.region,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
