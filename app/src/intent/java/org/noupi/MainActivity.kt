@file:Suppress("DEPRECATION")
package org.noupi

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout

class MainActivity : UiActivity() {
    private var qr: UpiQr? = null
    private var flow = HandoffState()
    private var raw = ""
    private var typedAmount = ""
    private var linkText = ""
    private var editingLink = false
    private var pausedAfterHandoff = false
    private var amountField: EditText? = null
    private val explanation = "NoUPI reads a small subset of unsigned, static UPI QR codes. Signed, dynamic and unfamiliar fields are rejected.\n\nQR names are unverified. Check the payee UPI ID and the payee shown by your payment app. Choose only an app you trust; appearing in the chooser is not certification.\n\nYour chosen app controls its full payment and authentication screens. NoUPI cannot verify payment status, even if an app reports success. Check there before starting another payment.\n\nScanning uses ZXing Android Embedded 4.3.0 and ZXing Core 3.5.4 (Apache 2.0). NoUPI source: github.com/anima-regem/NoUPI (MIT). No analytics, accounts, payment backend or stored payment history."

    override fun onCreate(savedInstanceState: Bundle?) {
        val state = savedInstanceState
        super.onCreate(state)
        raw = state?.getString("qr") ?: ""
        typedAmount = state?.getString("amount") ?: ""
        linkText = state?.getString("link") ?: ""
        editingLink = state?.getBoolean("editing") ?: false
        flow = HandoffState(state?.getString("stage")?.let { runCatching { HandoffState.Stage.valueOf(it) }.getOrNull() } ?: HandoffState.Stage.REVIEW)
        flow.restored()
        qr = runCatching { UpiParser.parse(raw) }.getOrNull()
        if (qr == null) home() else review()
    }
    override fun onSaveInstanceState(out: Bundle) {
        typedAmount = amountField?.text?.toString() ?: typedAmount
        linkText = findViewById<EditText>(R.id.link_input)?.text?.toString() ?: linkText
        out.putString("qr", raw); out.putString("amount", typedAmount); out.putString("stage", flow.stage.name)
        out.putString("link", linkText); out.putBoolean("editing", editingLink)
        super.onSaveInstanceState(out)
    }
    private fun home(error: String? = null) {
        qr = null; amountField = null
        page("Scan to pay") { help(explanation) }
        text("Review the payee, then choose your payment app.")
        space(34)
        column.addView(ImageView(this).apply {
            setImageResource(R.drawable.ic_scan)
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            setPadding(dp(32), dp(16), dp(32), dp(16))
        }, LinearLayout.LayoutParams(-1, dp(160)))
        space(20)
        button("Scan QR code", id = R.id.scan_button) { startActivityForResult(Intent(this, ScanActivity::class.java), 10) }
        button("Paste a UPI link", false) { editingLink = true; showLinkInput() }
        space(24)
        if (error != null) notice(error)
        if (editingLink) showLinkInput()
    }
    private fun showLinkInput() {
        if (findViewById<View>(R.id.link_input) != null) return
        space(20)
        val label = text("UPI link", 14, muted, true)
        val field = input(R.id.link_input, "upi://pay?pa=…", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_URI)
        label.labelFor = field.id; field.setText(linkText)
        button("Review payment", false, R.id.review_button) { linkText = field.text.toString(); accept(linkText) }
    }
    private fun accept(value: String) {
        try {
            val parsed = UpiParser.parse(value)
            raw = value; qr = parsed; typedAmount = ""; editingLink = false; linkText = ""
            flow = HandoffState(); review()
        } catch (e: InvalidQr) { home(e.message ?: "This QR is not supported.") }
    }
    private fun review(error: String? = null) {
        val payee = qr ?: return home()
        page("Review payment") { help(explanation) }
        text("QR name · unverified", 14)
        text(payee.name ?: "No name provided", 26, ink, true)
        space(22)
        text("Payee UPI ID", 14)
        text(payee.vpa, 20, ink).apply { typeface = Typeface.MONOSPACE; setTextIsSelectable(true); textDirection = View.TEXT_DIRECTION_LTR }
        space(28)
        amountField = null
        if (payee.amountPaise == null) {
            val label = text("Amount (INR)", 14, muted, true)
            amountField = input(R.id.amount_input, "0.00", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL).also {
                label.labelFor = it.id; it.setText(typedAmount)
                it.isEnabled = flow.stage == HandoffState.Stage.REVIEW
            }
        } else {
            text("Amount (INR)", 14)
            text("₹${payee.withAmount(payee.amountPaise).amount}", 42, ink, true)
        }
        payee.note?.let { space(22); text("Note from QR", 14); text(it, 18, ink) }
        space(28)
        if (flow.stage != HandoffState.Stage.REVIEW) {
            notice("Check your payment app\n\nNoUPI cannot confirm whether money moved. Check the actual payment there before starting another payment.")
            button("Start over", false, R.id.start_over) { clear() }
        } else {
            text("Check this UPI ID against the payee shown in the app you choose. That app handles payment and authentication.", 15)
            if (error != null) { space(16); notice(error) }
            button("Choose payment app", id = R.id.pay_button) { openPayment() }
            button("Use another QR", false, R.id.start_over) { clear() }
        }
    }
    private fun clear() { raw = ""; typedAmount = ""; linkText = ""; editingLink = false; flow = HandoffState(); home() }
    private fun openPayment() {
        val payee = qr ?: return
        val request = try {
            typedAmount = amountField?.text?.toString() ?: typedAmount
            payee.withAmount(payee.amountPaise ?: UpiParser.parseAmount(typedAmount))
        } catch (e: IllegalArgumentException) {
            amountField?.error = e.message; amountField?.requestFocus(); return
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(request.uri()))
        if (packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty()) {
            review("No app on this device can open this UPI payment. Install or enable a payment app you trust, then choose it here."); return
        }
        if (!flow.begin()) return
        try {
            pausedAfterHandoff = false
            startActivityForResult(Intent.createChooser(intent, "Choose a payment app you trust"), 20)
            review()
        } catch (_: ActivityNotFoundException) {
            flow.unavailable(); review("The payment app could not be opened. Choose an available app and try again.")
        } catch (_: SecurityException) {
            flow.unavailable(); review("Android blocked opening the payment app. Check its availability and try again.")
        }
    }
    override fun onPause() { if (flow.stage == HandoffState.Stage.OPENED) pausedAfterHandoff = true; super.onPause() }
    override fun onResume() {
        super.onResume()
        if (pausedAfterHandoff && flow.stage == HandoffState.Stage.OPENED) { flow.returned(); pausedAfterHandoff = false; review() }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) data?.getStringExtra("qr")?.let(::accept)
        if (requestCode == 20 && qr != null) { flow.returned(); review() } // Ignore all callback payloads and status codes.
    }
}
