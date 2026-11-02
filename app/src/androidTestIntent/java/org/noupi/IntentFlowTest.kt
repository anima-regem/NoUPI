package org.noupi

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class IntentFlowTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val qr = "upi://pay?pa=reader%40example&pn=Test%20payee&am=12.30&cu=INR&tn=Tea"
    private fun openReview(link: String = qr) {
        onView(withText("Paste a UPI link")).perform(click())
        onView(withId(R.id.link_input)).perform(scrollTo(), replaceText(link), closeSoftKeyboard())
        onView(withId(R.id.review_button)).perform(scrollTo(), click())
    }
    private fun screenshot(scenario: ActivityScenario<MainActivity>, name: String) {
        // Only fictional fixture content. Production keeps FLAG_SECURE set.
        scenario.onActivity { it.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
        val suffix = InstrumentationRegistry.getArguments().getString("screenshotSuffix", "")
        val file = File(context.filesDir, "$name$suffix.png")
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).waitForIdle()
        assertTrue(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).takeScreenshot(file))
    }
    private fun hasHandler() = context.packageManager.queryIntentActivities(Intent(Intent.ACTION_VIEW, Uri.parse(qr)), PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
    @Test fun reviewRejectsMalformedQrAndPreservesRecovery() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openReview("$qr&am=99")
            onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("repeats"))))
            onView(withId(R.id.link_input)).check(matches(withText("$qr&am=99")))
        }
    }
    @Test fun amountMustBeEnteredWhenQrOmitsIt() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openReview("upi://pay?pa=reader%40example")
            onView(withId(R.id.pay_button)).perform(scrollTo(), click())
            onView(withId(R.id.amount_input)).check(matches(hasErrorText("Enter a positive INR amount with at most two decimal places.")))
        }
    }
    @Test fun noHandlerIsRecoverable() {
        Assume.assumeFalse("Run once before installing the debug test handler", hasHandler())
        ActivityScenario.launch(MainActivity::class.java).use {
            openReview(); onView(withId(R.id.pay_button)).perform(scrollTo(), click())
            onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("No app on this device"))))
            onView(withId(R.id.pay_button)).check(matches(isEnabled()))
        }
    }
    @Test fun chooserPayloadExactlyMatchesReviewedFields() {
        Assume.assumeTrue(hasHandler())
        Intents.init()
        try {
            intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, Intent().putExtra("response", "Status=SUCCESS")))
            ActivityScenario.launch(MainActivity::class.java).use {
                openReview(); onView(withId(R.id.pay_button)).perform(scrollTo(), click())
                intended(hasAction(Intent.ACTION_CHOOSER))
                val chooser = Intents.getIntents().single { i -> i.action == Intent.ACTION_CHOOSER }
                @Suppress("DEPRECATION") val launched = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
                assertEquals(Intent.ACTION_VIEW, launched.action)
                assertEquals(UpiParser.parse(qr), UpiParser.parse(launched.dataString!!))
                onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("cannot confirm"))))
                onView(withId(R.id.pay_button)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
                it.recreate()
                onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("cannot confirm"))))
                assertEquals(1, Intents.getIntents().count { i -> i.action == Intent.ACTION_CHOOSER })
            }
        } finally { Intents.release() }
    }
    @Test fun cancelledCallbackCannotProduceRetryOrSuccess() {
        Assume.assumeTrue(hasHandler()); Intents.init()
        try {
            intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null))
            ActivityScenario.launch(MainActivity::class.java).use {
                openReview(); onView(withId(R.id.pay_button)).perform(scrollTo(), click())
                onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("cannot confirm"))))
                onView(withId(R.id.pay_button)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
            }
        } finally { Intents.release() }
    }
    @Test fun realDebugHandlerFakeSuccessAndMissingCallbackStayUnverified() {
        Assume.assumeTrue(hasHandler())
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        ActivityScenario.launch(MainActivity::class.java).use {
            openReview(); onView(withId(R.id.pay_button)).perform(scrollTo(), click())
            if (device.wait(Until.hasObject(By.text("NoUPI test handler")), 4000)) {
                device.findObject(By.text("NoUPI test handler")).click()
                device.wait(Until.hasObject(By.text("Just once")), 1000)
                device.findObject(By.text("Just once"))?.click()
            }
            assertTrue(device.wait(Until.hasObject(By.text("Return fake SUCCESS")), 5000))
            device.findObject(By.text("Return fake SUCCESS")).click()
            onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("cannot confirm"))))
            onView(withId(R.id.start_over)).perform(scrollTo(), click())
            openReview(); onView(withId(R.id.pay_button)).perform(scrollTo(), click())
            if (device.wait(Until.hasObject(By.text("NoUPI test handler")), 2000)) {
                device.findObject(By.text("NoUPI test handler")).click()
                device.wait(Until.hasObject(By.text("Just once")), 1000)
                device.findObject(By.text("Just once"))?.click()
            }
            assertTrue(device.wait(Until.hasObject(By.text("Stay open without callback")), 5000))
            device.findObject(By.text("Stay open without callback")).click()
            device.pressHome()
            context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onView(withId(R.id.notice)).check(matches(withText(org.hamcrest.Matchers.containsString("cannot confirm"))))
            onView(withId(R.id.pay_button)).check(androidx.test.espresso.assertion.ViewAssertions.doesNotExist())
        }
    }
    @Test fun recreatingReviewPreservesTypedAmountWithoutLaunching() {
        ActivityScenario.launch(MainActivity::class.java).use {
            openReview("upi://pay?pa=reader%40example&pn=Test%20payee")
            onView(withId(R.id.amount_input)).perform(scrollTo(), replaceText("2.35"), closeSoftKeyboard())
            it.recreate(); onView(withId(R.id.amount_input)).check(matches(withText("2.35")))
            onView(withId(R.id.pay_button)).check(matches(isEnabled()))
        }
    }
    @Test fun cameraDenialReturnsToPasteRecovery() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        Assume.assumeTrue("Denial path is exercised on the first fresh install", context.checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.scan_button)).perform(scrollTo(), click())
            onView(withText("Allow camera")).perform(scrollTo(), click())
            val deny = By.res("com.android.permissioncontroller", "permission_deny_button")
            assertTrue(device.wait(Until.hasObject(deny), 5000))
            device.findObject(deny).click()
            onView(withText("Camera access is off")).check(matches(isDisplayed()))
            onView(withText("Go back")).perform(scrollTo(), click())
            onView(withText("Paste a UPI link")).check(matches(isDisplayed()))
        }
    }
    @Test fun grantedCameraCanOpenResumeAndCancel() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.executeShellCommand("pm grant org.noupi.intent android.permission.CAMERA")
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.scan_button)).perform(scrollTo(), click())
            onView(withText("Cancel scan")).check(matches(isDisplayed()))
            device.pressHome()
            context.startActivity(context.packageManager.getLaunchIntentForPackage(context.packageName)!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onView(withText("Cancel scan")).perform(scrollTo(), click())
            onView(withId(R.id.scan_button)).check(matches(isDisplayed()))
        }
    }
    @Test fun screenshotsHomeAndReview() {
        ActivityScenario.launch(MainActivity::class.java).use {
            screenshot(it, "intent-home")
            openReview(); screenshot(it, "intent-review")
        }
    }
}
