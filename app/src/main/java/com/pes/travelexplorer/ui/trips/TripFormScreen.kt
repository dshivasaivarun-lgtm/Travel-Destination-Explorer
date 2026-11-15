package com.pes.travelexplorer.ui.trips

import androidx.compose.foundation.layout.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pes.travelexplorer.R
import com.pes.travelexplorer.data.local.TripEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormScreen(existingTrip: TripEntity? = null, onSave: (TripEntity) -> Unit,
    onCancel: () -> Unit, destinations: List<Pair<String, String>> = emptyList(),
    initialCountryCode: String = "", saving: Boolean = false, saveFailed: Boolean = false,
    loadingDestinations: Boolean = false, destinationLoadFailed: Boolean = false,
    onRetryDestinations: () -> Unit = {}) {
    BackHandler(enabled = saving) { /* Do not navigate away during the pending write. */ }
    var title by rememberSaveable(existingTrip?.id) { mutableStateOf(existingTrip?.title.orEmpty()) }
    var code by rememberSaveable(existingTrip?.id) { mutableStateOf(existingTrip?.destinationCountryCode ?: initialCountryCode) }
    var start by rememberSaveable(existingTrip?.id) { mutableStateOf(existingTrip?.startDate.orEmpty()) }
    var end by rememberSaveable(existingTrip?.id) { mutableStateOf(existingTrip?.endDate.orEmpty()) }
    var notes by rememberSaveable(existingTrip?.id) { mutableStateOf(existingTrip?.notes.orEmpty()) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    var selectingCountry by remember { mutableStateOf(false) }
    var selectingDate by remember { mutableStateOf<String?>(null) }
    val destinationName = destinations.firstOrNull { it.first == code }?.second
        ?: existingTrip?.takeIf { it.destinationCountryCode == code }?.destinationCountryName
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(if (existingTrip == null) R.string.plan_trip else R.string.edit_trip)) }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.trip_title)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, enabled = !saving)
            OutlinedButton(onClick = { selectingCountry = true }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text(destinationName ?: stringResource(R.string.country_selection))
            }
            if (loadingDestinations) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (destinationLoadFailed) {
                Text(stringResource(R.string.no_destinations))
                TextButton(onClick = onRetryDestinations, enabled = !loadingDestinations) { Text(stringResource(R.string.retry)) }
            }
            OutlinedButton(onClick = { selectingDate = "start" }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.start_date) + ": " + start.ifBlank { stringResource(R.string.select_date) })
            }
            OutlinedButton(onClick = { selectingDate = "end" }, enabled = !saving, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.end_date) + ": " + end.ifBlank { stringResource(R.string.select_date) })
            }
            OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.notes)) }, modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = !saving)
            if (invalid) Text(stringResource(R.string.trip_form_error), color = MaterialTheme.colorScheme.error)
            if (saveFailed) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            Button(enabled = !saving, modifier = Modifier.fillMaxWidth(), onClick = {
                invalid = title.isBlank() || destinationName.isNullOrBlank() ||
                    !code.matches(Regex("[A-Z]{3}")) || !validTripDates(start, end)
                if (!invalid) onSave(TripEntity(existingTrip?.id ?: 0, title.trim(), code,
                    destinationName.orEmpty(), start, end, notes.trim()))
            }) {
                if (saving) CircularProgressIndicator(Modifier.size(20.dp))
                else Text(stringResource(if (existingTrip == null) R.string.save_trip else R.string.update_trip))
            }
            TextButton(onClick = onCancel, enabled = !saving, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancel)) }
        }
    }
    if (selectingCountry) {
        var query by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { selectingCountry = false }, title = { Text(stringResource(R.string.country_selection)) },
            text = { Column {
                OutlinedTextField(query, { query = it }, label = { Text(stringResource(R.string.explore_search)) }, singleLine = true)
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(destinations.filter { it.second.contains(query, true) }, key = { it.first }) { destination ->
                        TextButton(onClick = { code = destination.first; selectingCountry = false }) { Text(destination.second) }
                    }
                }
            } }, confirmButton = { TextButton(onClick = { selectingCountry = false }) { Text(stringResource(R.string.cancel)) } })
    }
    selectingDate?.let { which ->
        val picker = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { selectingDate = null }, confirmButton = {
            TextButton(enabled = picker.selectedDateMillis != null, onClick = {
                picker.selectedDateMillis?.let { millis ->
                    val value = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(millis))
                    if (which == "start") start = value else end = value
                }
                selectingDate = null
            }) { Text(stringResource(R.string.confirm)) }
        }, dismissButton = { TextButton(onClick = { selectingDate = null }) { Text(stringResource(R.string.cancel)) } }) {
            DatePicker(picker)
        }
    }
}
