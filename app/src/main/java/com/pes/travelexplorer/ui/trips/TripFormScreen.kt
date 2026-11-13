
package com.pes.travelexplorer.ui.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pes.travelexplorer.data.local.TripEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormScreen(
    existingTrip: TripEntity? = null,
    onSave: (TripEntity) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember(existingTrip?.id) {
        mutableStateOf(existingTrip?.title ?: "")
    }

    var countryName by remember(existingTrip?.id) {
        mutableStateOf(existingTrip?.destinationCountryName ?: "")
    }

    var countryCode by remember(existingTrip?.id) {
        mutableStateOf(existingTrip?.destinationCountryCode ?: "")
    }

    var startDate by remember(existingTrip?.id) {
        mutableStateOf(existingTrip?.startDate ?: "")
    }

    var endDate by remember(existingTrip?.id) {
        mutableStateOf(existingTrip?.endDate ?: "")
    }

    var notes by remember(existingTrip?.id) {
        mutableStateOf(existingTrip?.notes ?: "")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingTrip == null) {
                            "Plan a New Trip"
                        } else {
                            "Edit Trip"
                        }
                    )
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Trip Title") },
                placeholder = { Text("Example: Summer Vacation") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = countryName,
                onValueChange = { countryName = it },
                label = { Text("Destination Country") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = countryCode,
                onValueChange = {
                    countryCode = it.uppercase().take(3)
                },
                label = { Text("Country Code (Alpha-3)") },
                placeholder = { Text("Example: IND") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = startDate,
                onValueChange = { startDate = it },
                label = { Text("Start Date") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = endDate,
                onValueChange = { endDate = it },
                label = { Text("End Date") },
                placeholder = { Text("YYYY-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Write your travel plans...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val validDateFormat =
                        Regex("""\d{4}-\d{2}-\d{2}""")

                    when {
                        title.isBlank() ->
                            errorMessage = "Please enter a trip title."

                        countryName.isBlank() ->
                            errorMessage = "Please enter a destination."

                        !countryCode.matches(Regex("[A-Z]{3}")) ->
                            errorMessage = "Enter a valid 3-letter country code."

                        !validDateFormat.matches(startDate) ||
                                !validDateFormat.matches(endDate) ->
                            errorMessage = "Dates must use YYYY-MM-DD format."

                        startDate > endDate ->
                            errorMessage = "End date cannot be before start date."

                        else -> {
                            errorMessage = null

                            onSave(
                                TripEntity(
                                    id = existingTrip?.id ?: 0,
                                    title = title.trim(),
                                    destinationCountryCode = countryCode,
                                    destinationCountryName = countryName.trim(),
                                    startDate = startDate,
                                    endDate = endDate,
                                    notes = notes.trim()
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (existingTrip == null) "Save Trip"
                    else "Update Trip"
                )
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }
}
