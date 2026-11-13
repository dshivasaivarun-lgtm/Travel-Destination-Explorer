
package com.pes.travelexplorer.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

import com.pes.travelexplorer.data.local.AppDatabase
import com.pes.travelexplorer.data.local.TripEntity
import com.pes.travelexplorer.data.local.WishlistEntity
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.data.repository.CountryRepository

import com.pes.travelexplorer.ui.factory.AppViewModelFactory
import com.pes.travelexplorer.ui.cities.CityScreen
import com.pes.travelexplorer.ui.details.CountryDetailScreen
import com.pes.travelexplorer.ui.map.TravelMapScreen
import com.pes.travelexplorer.ui.states.StateScreen
import com.pes.travelexplorer.ui.tourist.TouristPlacesScreen
import com.pes.travelexplorer.ui.trips.TripFormScreen
import com.pes.travelexplorer.ui.trips.TripsScreen
import com.pes.travelexplorer.ui.trips.TripsViewModel
import com.pes.travelexplorer.ui.wishlist.WishlistScreen
import com.pes.travelexplorer.ui.wishlist.WishlistViewModel
import com.pes.travelexplorer.utils.TravelIntents

import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun AppNavGraph() {

    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember {
        AppDatabase.getDatabase(context)
    }

    val factory = remember(database) {
        AppViewModelFactory(database)
    }

    val wishlistViewModel: WishlistViewModel = viewModel(
        factory = factory
    )

    val tripsViewModel: TripsViewModel = viewModel(
        factory = factory
    )

    NavHost(
        navController = navController,
        startDestination = "explore"
    ) {

        composable("explore") {
            ExploreDynamicScreen(
                onCountryClick = { countryCode ->
                    navController.navigate("country_details/$countryCode")
                },
                onWishlistClick = {
                    navController.navigate("wishlist")
                },
                onTripsClick = {
                    navController.navigate("trips")
                },
                wishlistViewModel = wishlistViewModel
            )
        }

        composable("wishlist") {
            WishlistScreen(
                viewModel = wishlistViewModel,
                onCountryClick = { countryCode ->
                    navController.navigate("country_details/$countryCode")
                }
            )
        }

        composable("trips") {
            TripsScreen(
                viewModel = tripsViewModel,
                onAddTrip = {
                    navController.navigate("trip_form")
                },
                onEditTrip = { tripId ->
                    navController.navigate("trip_form?tripId=$tripId")
                }
            )
        }

        composable(
            route = "trip_form?tripId={tripId}",
            arguments = listOf(
                navArgument("tripId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->

            val tripId =
                backStackEntry.arguments?.getInt("tripId") ?: -1

            var existingTrip by remember(tripId) {
                mutableStateOf<TripEntity?>(null)
            }

            LaunchedEffect(tripId) {
                if (tripId >= 0) {
                    tripsViewModel.getTripById(tripId) {
                        existingTrip = it
                    }
                }
            }

            TripFormScreen(
                existingTrip = existingTrip,
                onSave = { trip ->
                    if (tripId >= 0 && existingTrip != null) {
                        tripsViewModel.updateTrip(trip)
                    } else {
                        tripsViewModel.addTrip(trip)
                    }
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "country_details/{countryCode}",
            arguments = listOf(
                navArgument("countryCode") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val countryCode =
                backStackEntry.arguments?.getString("countryCode") ?: "IND"

            CountryDetailScreen(
                countryCode = countryCode,
                onBackClick = {
                    navController.popBackStack()
                },
                onViewStatesClick = { alpha2 ->
                    navController.navigate("states/$alpha2")
                }
            )
        }

        composable(
            route = "states/{countryCode}",
            arguments = listOf(
                navArgument("countryCode") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val countryCode =
                backStackEntry.arguments?.getString("countryCode") ?: "IN"

            StateScreen(
                countryCode = countryCode,
                onStateClick = { admin1Id, stateName ->
                    val encodedName = URLEncoder.encode(
                        stateName,
                        StandardCharsets.UTF_8.toString()
                    )
                    navController.navigate(
                        "cities/$countryCode/$admin1Id/$encodedName"
                    )
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "cities/{countryCode}/{admin1Id}/{stateName}",
            arguments = listOf(
                navArgument("countryCode") {
                    type = NavType.StringType
                },
                navArgument("admin1Id") {
                    type = NavType.StringType
                },
                navArgument("stateName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val countryCode =
                backStackEntry.arguments?.getString("countryCode") ?: "IN"

            val admin1Id =
                backStackEntry.arguments?.getString("admin1Id") ?: ""

            val encodedName =
                backStackEntry.arguments?.getString("stateName") ?: "State"

            val stateName = URLDecoder.decode(
                encodedName,
                StandardCharsets.UTF_8.toString()
            )

            CityScreen(
                countryCode = countryCode,
                admin1Id = admin1Id,
                stateName = stateName,
                onBackClick = {
                    navController.popBackStack()
                },
                onTouristPlacesClick = { cityName, lat, lon ->
                    val encodedCity = URLEncoder.encode(
                        cityName,
                        StandardCharsets.UTF_8.toString()
                    )
                    navController.navigate(
                        "tourist_places/$encodedCity/$lat/$lon"
                    )
                }
            )
        }

        composable(
            route = "tourist_places/{cityName}/{lat}/{lon}",
            arguments = listOf(
                navArgument("cityName") {
                    type = NavType.StringType
                },
                navArgument("lat") {
                    type = NavType.StringType
                },
                navArgument("lon") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val encodedCity =
                backStackEntry.arguments?.getString("cityName") ?: "City"

            val cityName = URLDecoder.decode(
                encodedCity,
                StandardCharsets.UTF_8.toString()
            )

            val lat =
                backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                    ?: 12.9716

            val lon =
                backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
                    ?: 77.5946

            TouristPlacesScreen(
                cityName = cityName,
                latitude = lat,
                longitude = lon,
                onBackClick = {
                    navController.popBackStack()
                },
                onViewOnMapClick = { name, latitude, longitude ->
                    val enc = URLEncoder.encode(
                        name,
                        StandardCharsets.UTF_8.toString()
                    )
                    navController.navigate(
                        "map/$enc/$latitude/$longitude"
                    )
                }
            )
        }

        composable(
            route = "map/{cityName}/{lat}/{lon}",
            arguments = listOf(
                navArgument("cityName") {
                    type = NavType.StringType
                },
                navArgument("lat") {
                    type = NavType.StringType
                },
                navArgument("lon") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val encodedCity =
                backStackEntry.arguments?.getString("cityName") ?: "City"

            val cityName = URLDecoder.decode(
                encodedCity,
                StandardCharsets.UTF_8.toString()
            )

            val lat =
                backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                    ?: 12.9716

            val lon =
                backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
                    ?: 77.5946

            TravelMapScreen(
                cityName = cityName,
                latitude = lat,
                longitude = lon,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}


// EXPLORE SCREEN

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun ExploreDynamicScreen(
    onCountryClick: (String) -> Unit,
    onWishlistClick: () -> Unit,
    onTripsClick: () -> Unit,
    wishlistViewModel: WishlistViewModel
) {

    var countries by remember {
        mutableStateOf<List<Country>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val savedCountries by wishlistViewModel.wishlist.collectAsState()

    val savedCountryCodes = remember(savedCountries) {
        savedCountries.map { it.cca3 }.toSet()
    }

    fun loadCountries() {
        scope.launch {

            isLoading = true
            errorMessage = null

            val result = CountryRepository().getAllCountries()

            result.fold(
                onSuccess = { list ->
                    countries = list.sortedBy {
                        it.names?.common
                    }
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage =
                        error.localizedMessage ?: "Failed to load countries"
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
                title = {
                    Text("Travel Explorer - All Countries")
                },
                actions = {
                    TextButton(onClick = onWishlistClick) {
                        Text("Wishlist")
                    }

                    TextButton(onClick = onTripsClick) {
                        Text("Trips")
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
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search all countries...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {

                isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errorMessage ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(onClick = { loadCountries() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                else -> {

                    val filtered = countries.filter {
                        it.names?.common?.contains(
                            searchQuery,
                            ignoreCase = true
                        ) == true ||
                                it.region?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true
                    }

                    if (filtered.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No countries found.")
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            items(
                                items = filtered,
                                key = {
                                    it.codes?.alpha3
                                        ?: it.names?.common.orEmpty()
                                }
                            ) { country ->

                                val alpha3 = country.codes?.alpha3

                                val isWishlisted =
                                    !alpha3.isNullOrBlank() &&
                                            alpha3 in savedCountryCodes

                                CountryCardItem(
                                    country = country,
                                    isWishlisted = isWishlisted,

                                    onWishlistClick = {
                                        if (!alpha3.isNullOrBlank()) {
                                            if (isWishlisted) {
                                                wishlistViewModel.removeCountry(alpha3)
                                            } else {
                                                wishlistViewModel.addCountry(
                                                    WishlistEntity(
                                                        cca3 = alpha3,
                                                        countryName = country.names?.common
                                                            ?: "Unknown",
                                                        officialName = country.names?.official
                                                            ?: country.names?.common
                                                            ?: "Unknown",
                                                        flagUrl = country.flag?.urlPng
                                                            ?: country.flag?.urlSvg,
                                                        region = country.region
                                                    )
                                                )
                                            }
                                        }
                                    },

                                    onShareClick = {
                                        val countryName =
                                            country.names?.common ?: "Unknown"

                                        TravelIntents.shareDestination(
                                            context,
                                            countryName
                                        )
                                    },

                                    onClick = {
                                        if (!alpha3.isNullOrBlank()) {
                                            onCountryClick(alpha3)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// COUNTRY CARD

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun CountryCardItem(
    country: Country,
    isWishlisted: Boolean,
    onWishlistClick: () -> Unit,
    onShareClick: () -> Unit,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val flagUrl = country.flag?.urlPng
                ?: country.flag?.urlSvg

            if (!flagUrl.isNullOrBlank()) {

                Card(
                    modifier = Modifier.size(56.dp, 36.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 1.dp
                    )
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

            Column(
                modifier = Modifier.weight(1f)
            ) {

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

            // Share button
            IconButton(
                onClick = onShareClick
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share destination"
                )
            }

            // Wishlist button
            IconButton(
                onClick = onWishlistClick
            ) {
                Icon(
                    imageVector = if (isWishlisted) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = if (isWishlisted) {
                        "Remove from wishlist"
                    } else {
                        "Add to wishlist"
                    },
                    tint = if (isWishlisted) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
