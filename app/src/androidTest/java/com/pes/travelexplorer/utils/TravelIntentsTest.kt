package com.pes.travelexplorer.utils

import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test

/** Exercise the external-app boundary without sending anything to another person. */
class TravelIntentsTest {
    private class CapturingContext(private val reject: Boolean = false) : ContextWrapper(
        InstrumentationRegistry.getInstrumentation().targetContext
    ) {
        val attempts = mutableListOf<Intent>()
        override fun startActivity(intent: Intent) {
            attempts.add(intent)
            if (reject) throw ActivityNotFoundException("No handler installed")
        }
    }

    @Test fun researchUsesAnImplicitWebIntent() {
        val context = CapturingContext()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            TravelIntents.openWebsite(context, "https://example.com/travel?q=Japan")
        }
        val intent = context.attempts.single()
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals("https", intent.data?.scheme)
        assertNull(intent.component)
    }

    @Suppress("DEPRECATION")
    @Test fun destinationSharingUsesChooserAndPlainText() {
        val context = CapturingContext()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            TravelIntents.shareDestination(context, "Japan")
        }
        val chooser = context.attempts.single()
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("text/plain", send.type)
        assertTrue(send.getStringExtra(Intent.EXTRA_TEXT).orEmpty().contains("Japan"))
        assertNull(send.component)
    }

    @Test fun missingExternalAppsDoNotCrashEitherAction() {
        val context = CapturingContext(reject = true)
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            TravelIntents.openWebsite(context, "https://example.com/travel")
            TravelIntents.shareDestination(context, "Japan")
        }
        assertEquals(2, context.attempts.size)
    }

    @Test fun unsupportedOrHostlessLinksDoNotLaunchAnApp() {
        val context = CapturingContext()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            listOf("file:///photo.jpg", "javascript:alert(1)", "https:///missing-host").forEach {
                TravelIntents.openWebsite(context, it)
            }
        }
        assertTrue(context.attempts.isEmpty())
    }
}
