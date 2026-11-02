package org.noupi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle

class MainActivity : UiActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val state = savedInstanceState
        super.onCreate(state); show()
    }
    private fun show(error: String? = null) {
        page("Banking with *99#") {
            help("NoUPI opens *99# in your phone's dialler. You decide whether to dial it and which SIM to use.\n\nAvailability, registration, services and charges depend on your bank, SIM and carrier. Use the mobile number registered with your bank.\n\nSystem and carrier menus handle the banking session. NoUPI cannot read, fill or control these menus and does not handle PINs. This variant does not transfer QR payee details into a USSD session.\n\nNoUPI source: github.com/anima-regem/NoUPI (MIT). No accounts, analytics, payment backend or payment history.")
        }
        space(24)
        text("*99#", 72, accent, true)
        space(24)
        text("Open the dialler, choose your bank-linked SIM, then dial when you’re ready.", 19, ink)
        space(20)
        text("Your bank and carrier provide the menus. Availability and charges depend on your SIM, bank and carrier.")
        space(28)
        button("Open dialler", id = R.id.dial_button) {
            try { startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "*99#", null))) }
            catch (_: ActivityNotFoundException) { show("No dialler is available on this device. Use a phone with a supported SIM and dial *99# yourself.") }
            catch (_: SecurityException) { show("Android blocked the dialler. Open your phone’s dialler and enter *99# yourself.") }
        }
        if (error != null) { space(20); notice(error) }
    }
}
