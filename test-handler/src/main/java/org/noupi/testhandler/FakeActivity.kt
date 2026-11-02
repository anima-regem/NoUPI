package org.noupi.testhandler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*

/** Debug fixture, never a banking UI. Does not make network requests or move money. */
class FakeActivity : Activity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(40, 80, 40, 40) }
        layout.addView(TextView(this).apply { text = "NoUPI test handler — SIMULATION\nNo money can move."; textSize = 24f })
        layout.addView(TextView(this).apply { text = intent.dataString ?: "No payload"; textSize = 16f })
        fun action(label: String, block: () -> Unit) { layout.addView(Button(this).apply { text = label; setOnClickListener { block() } }) }
        action("Return fake SUCCESS") { setResult(RESULT_OK, Intent().putExtra("response", "Status=SUCCESS&txnId=FAKE")); finish() }
        action("Return cancelled") { setResult(RESULT_CANCELED); finish() }
        action("Stay open without callback") { layout.addView(TextView(this).apply { text = "No callback sent. Press system Back or leave this app." }) }
        setContentView(layout)
    }
}
