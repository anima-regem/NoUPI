package org.noupi

import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.net.URLEncoder

class InvalidQr(message: String) : IllegalArgumentException(message)

data class UpiQr(val vpa: String, val name: String?, val amountPaise: Long?, val note: String?) {
    fun withAmount(paise: Long): PaymentRequest {
        require(paise in 1..UpiParser.MAX_PAISE)
        require(amountPaise == null || amountPaise == paise)
        return PaymentRequest(this, paise)
    }
}

class PaymentRequest internal constructor(val qr: UpiQr, val amountPaise: Long) {
    val amount: String get() = BigDecimal.valueOf(amountPaise, 2).toPlainString()
    fun uri(): String {
        val values = linkedMapOf("pa" to qr.vpa)
        qr.name?.let { values["pn"] = it }
        values["am"] = amount
        values["cu"] = "INR"
        qr.note?.let { values["tn"] = it }
        return "upi://pay?" + values.entries.joinToString("&") { (k, v) -> "$k=${encode(v)}" }
    }
    private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
}

/** Deliberately small, unsigned, static subset. Unknown fields are rejected, never stripped. */
object UpiParser {
    const val MAX_PAISE = 10_000_000L // Product cap ₹100,000; not a bank/UPI limit.
    private val allowed = setOf("pa", "pn", "am", "cu", "tn")
    private val vpaPattern = Regex("[A-Za-z0-9][A-Za-z0-9._-]{0,255}@[A-Za-z][A-Za-z0-9.-]{1,63}")
    private val amountPattern = Regex("(?:0|[1-9][0-9]{0,5})(?:\\.[0-9]{1,2})?")

    fun parse(raw: String): UpiQr {
        if (raw.length > 2048 || !raw.startsWith("upi://pay?") || raw.length <= 10) fail("Use a static upi://pay QR code or link.")
        if (raw.any { it.code !in 0x21..0x7e } || '#' in raw || raw.count { it == '?' } != 1) fail("The QR contains unsupported characters or a fragment.")
        val fields = linkedMapOf<String, String>()
        for (part in raw.substring(10).split('&')) {
            val equal = part.indexOf('=')
            if (equal <= 0 || part.indexOf('=', equal + 1) != -1) fail("The QR has a malformed field.")
            val key = decode(part.substring(0, equal))
            if (key !in allowed) fail("This QR uses fields NoUPI cannot safely handle. Open it in your payment app.")
            if (fields.containsKey(key)) fail("The QR repeats a field. Open it in your payment app.")
            val value = decode(part.substring(equal + 1))
            if (value.isEmpty() || unsafe(value)) fail("The QR contains an empty or unsafe field.")
            fields[key] = value
        }
        val vpa = fields["pa"] ?: fail("The QR is missing the payee UPI ID.")
        if (!vpaPattern.matches(vpa)) fail("The payee UPI ID is not supported.")
        if (fields["cu"] != null && fields["cu"] != "INR") fail("Only INR is supported.")
        val name = fields["pn"]?.also { if (it.length > 120 || it != it.trim()) fail("The QR name is too long or has surrounding spaces.") }
        val note = fields["tn"]?.also { if (it.length > 140 || it != it.trim()) fail("The note is too long or has surrounding spaces.") }
        return UpiQr(vpa, name, fields["am"]?.let(::parseAmount), note)
    }

    fun parseAmount(value: String): Long {
        if (!amountPattern.matches(value)) fail("Enter a positive INR amount with at most two decimal places.")
        val paise = BigDecimal(value).movePointRight(2).longValueExact()
        if (paise !in 1..MAX_PAISE) fail("Enter an amount from ₹0.01 to ₹100,000.00.")
        return paise
    }

    private fun unsafe(value: String): Boolean = value.codePoints().anyMatch {
        Character.isISOControl(it) || Character.getType(it) == Character.FORMAT.toInt() ||
            Character.getType(it) == Character.LINE_SEPARATOR.toInt() || Character.getType(it) == Character.PARAGRAPH_SEPARATOR.toInt() ||
            it in 0x202a..0x202e || it in 0x2066..0x2069 || it == 0xfffd
    }

    private fun decode(raw: String): String {
        val bytes = ByteArrayOutputStream()
        var i = 0
        while (i < raw.length) {
            when (val c = raw[i]) {
                '%' -> {
                    if (i + 2 >= raw.length) fail("The QR contains malformed percent encoding.")
                    val high = raw[i + 1].digitToIntOrNull(16) ?: fail("The QR contains malformed percent encoding.")
                    val low = raw[i + 2].digitToIntOrNull(16) ?: fail("The QR contains malformed percent encoding.")
                    val hex = high * 16 + low
                    bytes.write(hex); i += 3
                }
                '+' -> fail("The QR uses ambiguous plus encoding. Use %20 for spaces.")
                else -> { bytes.write(c.code); i++ }
            }
        }
        return try {
            StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes.toByteArray())).toString()
        } catch (_: java.nio.charset.CharacterCodingException) { fail("The QR contains invalid UTF-8.") }
    }
    private fun fail(message: String): Nothing = throw InvalidQr(message)
}
