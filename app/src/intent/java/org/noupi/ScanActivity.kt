package org.noupi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeView
import com.journeyapps.barcodescanner.CameraPreview
import com.journeyapps.barcodescanner.DefaultDecoderFactory

class ScanActivity : UiActivity() {
    private var scanner: BarcodeView? = null
    private var delivered = false
    override fun onCreate(savedInstanceState: Bundle?) {
        val state = savedInstanceState
        super.onCreate(state)
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) showScanner()
        else {
            page("Scan a QR code") { help("The camera reads QR codes on your device. You can also paste a UPI link without camera access.") }
            text("Allow camera access to read the QR code. You can paste a UPI link instead.")
            button("Allow camera") { requestPermissions(arrayOf(Manifest.permission.CAMERA), 1) }
            button("Go back", false) { finish() }
        }
    }
    private fun showScanner() {
        page("Scan a QR code") { help("Point the camera at a static UPI QR code. NoUPI will show its details before opening another app.") }
        text("Point your camera at the QR code.")
        space(24)
        scanner = BarcodeView(this).apply {
            decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))
            contentDescription = "QR camera preview"
            addStateListener(object : CameraPreview.StateListener {
                override fun previewSized() = Unit
                override fun previewStarted() = Unit
                override fun previewStopped() = Unit
                override fun cameraClosed() = Unit
                override fun cameraError(error: Exception) {
                    this@ScanActivity.scanner?.pause()
                    this@ScanActivity.scanner = null
                    page("Camera unavailable") { help("Close any other camera app and try again, or paste a UPI link on the previous screen.") }
                    text("Close any other camera app and try again, or go back to paste a UPI link.")
                    button("Go back") { finish() }
                }
            })
            column.addView(this, LinearLayout.LayoutParams(-1, dp(300)))
            decodeSingle { result ->
                if (!delivered) {
                    delivered = true; pause()
                    setResult(Activity.RESULT_OK, Intent().putExtra("qr", result.text)); finish()
                }
            }
            resume()
        }
        button("Cancel scan", false) { finish() }
    }
    override fun onRequestPermissionsResult(code: Int, permissions: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, permissions, results)
        if (code == 1 && results.firstOrNull() == PackageManager.PERMISSION_GRANTED) showScanner()
        else {
            page("Camera access is off") { help("You can enable camera access in Android settings, or paste a UPI link on the previous screen.") }
            text("Go back to paste a UPI link. You can enable camera access in Android settings later.")
            button("Go back") { finish() }
        }
    }
    override fun onResume() { super.onResume(); if (!delivered) scanner?.resume() }
    override fun onPause() { scanner?.pause(); super.onPause() }
}
