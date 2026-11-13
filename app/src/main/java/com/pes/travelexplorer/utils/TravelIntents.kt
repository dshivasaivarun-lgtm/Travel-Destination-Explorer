
package com.pes.travelexplorer.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object TravelIntents {

    // Intent 1: Share a travel destination
    fun shareDestination(context: Context, destination: String) {

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"

            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this travel destination: $destination"
            )
        }

        val chooser = Intent.createChooser(
            shareIntent,
            "Share destination using"
        )

        context.startActivity(chooser)
    }

    // Intent 2: Open a website
    fun openWebsite(context: Context, websiteUrl: String) {

        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(websiteUrl)
        )

        context.startActivity(browserIntent)
    }
}