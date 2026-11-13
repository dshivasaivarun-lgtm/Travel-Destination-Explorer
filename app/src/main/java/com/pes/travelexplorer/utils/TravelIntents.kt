package com.pes.travelexplorer.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.pes.travelexplorer.R

object TravelIntents {
    fun shareDestination(context: Context, destination: String) {
        shareText(context, context.getString(R.string.share_destination_text, destination))
    }
    fun shareText(context: Context, text: String) {
        launch(context, Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }, context.getString(R.string.share_destination)))
    }
    fun openWebsite(context: Context, websiteUrl: String) {
        val uri = Uri.parse(websiteUrl)
        if (uri.scheme !in listOf("https", "http") || uri.host.isNullOrBlank()) {
            Toast.makeText(context, R.string.external_app_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        launch(context, Intent(Intent.ACTION_VIEW, uri))
    }
    private fun launch(context: Context, intent: Intent) {
        try { context.startActivity(intent) }
        catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.external_app_unavailable, Toast.LENGTH_SHORT).show()
        } catch (_: SecurityException) {
            Toast.makeText(context, R.string.external_app_unavailable, Toast.LENGTH_SHORT).show()
        }
    }
}
