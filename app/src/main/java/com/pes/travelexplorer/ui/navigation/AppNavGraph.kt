package com.pes.travelexplorer.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.local.AppDatabase
import com.pes.travelexplorer.data.local.WishlistEntity
import com.pes.travelexplorer.data.model.Country
import com.pes.travelexplorer.ui.factory.AppViewModelFactory
import com.pes.travelexplorer.ui.home.detailsCode
import com.pes.travelexplorer.ui.trips.*
import com.pes.travelexplorer.ui.wishlist.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pes.travelexplorer.ui.cities.CityScreen
import com.pes.travelexplorer.ui.details.CountryDetailScreen
import com.pes.travelexplorer.ui.home.HomeScreen
import com.pes.travelexplorer.ui.map.TravelMapScreen
import com.pes.travelexplorer.ui.states.StateScreen
import com.pes.travelexplorer.ui.tourist.TouristPlacesScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val factory = remember(database) { AppViewModelFactory(database) }
    val wishlist: WishlistViewModel = viewModel(factory = factory)
    val trips: TripsViewModel = viewModel(factory = factory)
    val saved by wishlist.wishlist.collectAsState()
    val savedCodes = saved.map { it.cca3 }.toSet()
    fun toggle(country: Country) {
        val code = country.detailsCode() ?: return
        if (code in savedCodes) wishlist.removeCountry(code)
        else wishlist.addCountry(WishlistEntity(code, country.names?.common.orEmpty(),
            country.names?.official.orEmpty(), country.flag?.urlPng, country.region))
    }
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val destinations = listOf("explore" to R.string.nav_explore, "wishlist" to R.string.wishlist_title, "trips" to R.string.trips_title)
    Scaffold(bottomBar = {
        if (route?.startsWith("trip_form") != true) NavigationBar {
            destinations.forEach { (target, label) ->
                NavigationBarItem(selected = route == target,
                    onClick = { navController.navigate(target) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } }, icon = { Icon(when(target) { "explore" -> Icons.Default.Explore; "wishlist" -> Icons.Default.Favorite; else -> Icons.Default.Flight }, contentDescription = null) },
                    label = { Text(stringResource(label)) })
            }
        }
    }) { rootPadding ->
        Column(Modifier.fillMaxSize().padding(bottom = rootPadding.calculateBottomPadding()).consumeWindowInsets(PaddingValues(bottom = rootPadding.calculateBottomPadding()))) {
            val wishlistError by wishlist.error.collectAsState()
            val tripError by trips.error.collectAsState()
            val error = wishlistError ?: tripError
            if (error != null) Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text(stringResource(R.string.storage_error), modifier = Modifier.weight(1f))
                TextButton(onClick = { wishlist.clearError(); trips.clearError() }) { Text(stringResource(R.string.dismiss)) }
            }
            NavHost(
                navController = navController,
                startDestination = "explore"
            ) {
                composable("explore") {
                    HomeScreen(
                        onCountryClick = { countryCode -> navController.navigate("country_details/$countryCode") },
                        savedCodes = savedCodes, onToggleSaved = ::toggle
                    )
                }
                composable("wishlist") {
                    WishlistScreen(viewModel = wishlist,
                        onCountryClick = { navController.navigate("country_details/$it") },
                        onPlanTrip = { navController.navigate("trip_form?countryCode=$it") })
                }
                composable("trips") {
                    TripsScreen(viewModel = trips,
                        onAddTrip = { navController.navigate("trip_form") },
                        onEditTrip = { navController.navigate("trip_form?tripId=$it") })
                }
                composable("trip_form?tripId={tripId}&countryCode={countryCode}", arguments = listOf(
                    navArgument("tripId") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("countryCode") { type = NavType.StringType; defaultValue = "" }
                )) { formEntry ->
                    TripFormRoute(trips, formEntry.arguments?.getInt("tripId") ?: -1,
                        formEntry.arguments?.getString("countryCode").orEmpty(), saved,
                        onSaved = { navController.popBackStack(); navController.navigate("trips") { launchSingleTop = true } },
                        onCancel = { navController.popBackStack() })
                }
                composable(
                    route = "country_details/{countryCode}",
                    arguments = listOf(navArgument("countryCode") { type = NavType.StringType })
                ) { backStackEntry ->
                    val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "IND"
                    CountryDetailScreen(
                        countryCode = countryCode,
                        isSaved = countryCode in savedCodes,
                        onToggleSaved = ::toggle,
                        onPlanTrip = { navController.navigate("trip_form?countryCode=$countryCode") },
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
                    val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                    val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
                    if (lat == null || lon == null || !com.pes.travelexplorer.utils.validCoordinates(lat, lon)) {
                        Text(stringResource(R.string.location_unavailable))
                        return@composable
                    }
                    TouristPlacesScreen(
                        cityName = cityName,
                        latitude = lat,
                        longitude = lon,
                        onBackClick = { navController.popBackStack() },
                        onViewOnMapClick = { name, latitude, longitude, radius ->
                            val enc = URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
                            navController.navigate("map/$enc/$latitude/$longitude?radius=$radius")
                        }
                    )
                }
                composable(
                    route = "map/{cityName}/{lat}/{lon}?radius={radius}",
                    arguments = listOf(
                        navArgument("cityName") { type = NavType.StringType },
                        navArgument("lat") { type = NavType.StringType },
                        navArgument("lon") { type = NavType.StringType },
                        navArgument("radius") { type = NavType.IntType; defaultValue = 5000 }
                    )
                ) { backStackEntry ->
                    val encodedCity = backStackEntry.arguments?.getString("cityName") ?: "City"
                    val cityName = URLDecoder.decode(encodedCity, StandardCharsets.UTF_8.toString())
                    val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
                    val lon = backStackEntry.arguments?.getString("lon")?.toDoubleOrNull()
                    if (lat == null || lon == null || !com.pes.travelexplorer.utils.validCoordinates(lat, lon)) {
                        Text(stringResource(R.string.location_unavailable))
                        return@composable
                    }
                    TravelMapScreen(
                        cityName = cityName,
                        latitude = lat,
                        longitude = lon,
                        radiusMeters = backStackEntry.arguments?.getInt("radius") ?: 5000,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
