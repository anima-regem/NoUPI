package org.noupi

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.WindowInsets
import android.os.Build
import android.widget.*

@SuppressLint("SetTextI18n")
open class UiActivity : Activity() {
    protected val ink = Color.rgb(20, 44, 59)
    protected val muted = Color.rgb(82, 105, 120)
    protected val accent = Color.rgb(0, 103, 121)
    protected val paper = Color.rgb(242, 246, 250)
    protected lateinit var column: LinearLayout
    protected fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prevent screenshots and recent-task previews from retaining payee details.
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
    protected fun page(title: String, help: () -> Unit) {
        val scroll = ScrollView(this).apply { setBackgroundColor(paper); isFillViewport = true }
        column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(16), dp(24), dp(28)) }
        scroll.addView(column)
        scroll.setOnApplyWindowInsetsListener { v, insets ->
            if (Build.VERSION.SDK_INT >= 30) {
                val safe = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout() or WindowInsets.Type.ime())
                v.setPadding(safe.left, safe.top, safe.right, safe.bottom)
            } else {
                @Suppress("DEPRECATION")
                v.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            }
            insets
        }
        setContentView(scroll)
        val top = LinearLayout(this).apply { gravity = android.view.Gravity.CENTER_VERTICAL }
        top.addView(label("NoUPI", 19, ink, true), LinearLayout.LayoutParams(0, dp(52), 1f))
        top.addView(Button(this).apply {
            text = "Help"; setTextColor(accent); isAllCaps = false; backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            minWidth = dp(64); minHeight = dp(48); setOnClickListener { help() }
        })
        column.addView(top)
        space(30)
        text(title, 36, ink, true)
        space(14)
    }
    protected fun label(value: String, size: Int = 16, color: Int = muted, bold: Boolean = false): TextView = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color)
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        setLineSpacing(dp(3).toFloat(), 1f)
    }
    protected fun text(value: String, size: Int = 16, color: Int = muted, bold: Boolean = false): TextView = label(value, size, color, bold).also {
        column.addView(it, LinearLayout.LayoutParams(-1, -2))
    }
    protected fun space(height: Int) { column.addView(Space(this), LinearLayout.LayoutParams(1, dp(height))) }
    protected fun button(value: String, primary: Boolean = true, id: Int = View.NO_ID, action: () -> Unit): Button = Button(this).apply {
        text = value; this.id = id; isAllCaps = false; textSize = 16f
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        setTextColor(if (primary) Color.WHITE else accent)
        minHeight = dp(56); minimumHeight = dp(56)
        background = GradientDrawable().apply { setColor(if (primary) ink else Color.WHITE); cornerRadius = dp(16).toFloat() }
        setPadding(dp(16), dp(12), dp(16), dp(12))
        stateListAnimator = null
        setOnClickListener { action() }
        column.addView(this, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
    }
    protected fun input(id: Int, hint: String, kind: Int): EditText = EditText(this).apply {
        this.id = id; this.hint = hint; inputType = kind; setSingleLine(); textSize = 18f
        setTextColor(ink); setHintTextColor(muted); setPadding(dp(12), dp(12), dp(12), dp(12)); minHeight = dp(56)
        backgroundTintList = ColorStateList.valueOf(accent)
        importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        column.addView(this, LinearLayout.LayoutParams(-1, -2))
    }
    protected fun notice(value: String): TextView = text(value).apply {
        id = R.id.notice; accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        setTextColor(ink); setPadding(dp(16), dp(16), dp(16), dp(16))
        background = GradientDrawable().apply { setColor(Color.rgb(221, 237, 240)); cornerRadius = dp(12).toFloat() }
    }
    protected fun help(message: String) { AlertDialog.Builder(this).setTitle("About NoUPI").setMessage(message).setPositiveButton("Close", null).show() }
}
