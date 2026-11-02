package org.noupi

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UssdFlowTest {
    @Test fun diallerOpensOnlyOnTapAndNeverCalls() {
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_DIAL)).respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
            ActivityScenario.launch(MainActivity::class.java).use {
                assertEquals(0, Intents.getIntents().count { i -> i.action == Intent.ACTION_DIAL })
                onView(withId(R.id.dial_button)).perform(scrollTo(), click())
                val sent = Intents.getIntents().single { i -> i.action == Intent.ACTION_DIAL }
                assertEquals(Intent.ACTION_DIAL, sent.action)
                assertEquals("tel", sent.data!!.scheme)
                assertEquals("*99#", sent.data!!.schemeSpecificPart)
                assertNull(sent.data!!.fragment)
                it.recreate()
                assertEquals(1, Intents.getIntents().count { i -> i.action == Intent.ACTION_DIAL })
            }
        } finally { Intents.release() }
    }
    @Test fun screenshotHome() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(MainActivity::class.java).use {
            it.onActivity { a -> a.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            device.waitForIdle()
            assertTrue(device.takeScreenshot(File(context.filesDir, "ussd-home${InstrumentationRegistry.getArguments().getString("screenshotSuffix", "")}.png")))
        }
    }
}
