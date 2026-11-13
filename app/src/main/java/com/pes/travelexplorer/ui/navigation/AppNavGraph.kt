package com.pes.travelexplorer.ui.navigation

import androidx.compose.runtime.Composable
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

    NavHost(
        navController = navController,
        startDestination = "explore"
    ) {
        composable("explore") {
            HomeScreen(
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
