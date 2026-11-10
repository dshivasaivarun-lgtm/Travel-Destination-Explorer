package com.pes.travelexplorer.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pes.travelexplorer.ui.cities.CityScreen
import com.pes.travelexplorer.ui.details.CountryDetailScreen
import com.pes.travelexplorer.ui.states.StateScreen
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
            ExploreTestScreen(
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
                navArgument("stateName") { type = NavType.StringType })
        ) { backStackEntry ->
            val countryCode = backStackEntry.arguments?.getString("countryCode") ?: "IN"
            val admin1Id = backStackEntry.arguments?.getString("admin1Id") ?: ""
            val encodedName = backStackEntry.arguments?.getString("stateName") ?: "State"
            val stateName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())
            CityScreen(
                countryCode = countryCode,
                admin1Id = admin1Id,
                stateName = stateName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreTestScreen(onCountryClick: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Travel Explorer - Geographic Browse") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select a Country to Explore",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            val testCountries = listOf(
                "IND" to "India 🇮🇳",
                "JPN" to "Japan 🇯🇵",
                "FRA" to "France 🇫🇷",
                "USA" to "United States 🇺🇸"
            )

            testCountries.forEach { (code, name) ->
                Button(
                    onClick = { onCountryClick(code) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = name, style = MaterialTheme.typography.bodyLarge)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
