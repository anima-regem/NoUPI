package org.noupi

import org.junit.Assert.*
import org.junit.Test

class UpiParserTest {
    private val base = "upi://pay?pa=reader%40example"
    private fun rejected(uri: String) { assertThrows(InvalidQr::class.java) { UpiParser.parse(uri) } }
    @Test fun minimalQrNeedsUserAmount() {
        val qr = UpiParser.parse(base)
        assertEquals("reader@example", qr.vpa); assertNull(qr.name); assertNull(qr.amountPaise)
    }
    @Test fun unicodeNameAndNoteRoundTrip() {
        val qr = UpiParser.parse("$base&pn=%E0%B4%AE%E0%B4%A8%E0%B5%81&tn=Tea%20%26%20snacks&am=12.30&cu=INR")
        assertEquals("മനു", qr.name); assertEquals("Tea & snacks", qr.note); assertEquals(1230L, qr.amountPaise)
        assertEquals(qr, UpiParser.parse(qr.withAmount(1230).uri()))
    }
    @Test fun amountIsNeverFloatingPoint() { assertEquals(29L, UpiParser.parseAmount("0.29")); assertEquals(10_000_000L, UpiParser.parseAmount("100000.00")) }
    @Test fun missingAmountBecomesExplicitlyReviewedAmount() {
        val request = UpiParser.parse(base).withAmount(100)
        assertEquals("1.00", request.amount)
        assertEquals("upi://pay?pa=reader%40example&am=1.00&cu=INR", request.uri())
    }
    @Test fun qrAmountCannotBeOverridden() { assertThrows(IllegalArgumentException::class.java) { UpiParser.parse("$base&am=10").withAmount(2000) } }
    @Test fun acceptsOptionalInrAndTwoDecimals() { assertEquals(105L, UpiParser.parse("$base&cu=INR&am=1.05").amountPaise) }
    @Test fun duplicateFieldRejected() { rejected("$base&pa=other%40example") }
    @Test fun encodedDuplicateRejected() { rejected("$base&p%61=other%40example") }
    @Test fun duplicateNameRejected() { rejected("$base&pn=A&pn=B") }
    @Test fun mixedCaseKeyRejected() { rejected("$base&PA=other%40example") }
    @Test fun signatureRejected() { rejected("$base&sign=abc") }
    @Test fun dynamicReferenceRejected() { rejected("$base&tr=order123") }
    @Test fun securityModeRejected() { rejected("$base&mode=02") }
    @Test fun unknownFieldRejected() { rejected("$base&url=https%3A%2F%2Fexample.com") }
    @Test fun noHttpRedirects() { rejected("https://example.com/upi?pa=reader@example") }
    @Test fun noIntentUris() { rejected("intent://pay?pa=reader@example#Intent;scheme=upi;end") }
    @Test fun noAlternateAuthority() { rejected("upi://pay.evil?pa=reader@example") }
    @Test fun noUserInfo() { rejected("upi://evil@pay?pa=reader@example") }
    @Test fun noPathSuffix() { rejected("upi://pay/extra?pa=reader@example") }
    @Test fun noFragment() { rejected("$base#ignored") }
    @Test fun noSecondQuery() { rejected("$base&pn=A?B") }
    @Test fun noMissingPayee() { rejected("upi://pay?pn=Nobody") }
    @Test fun noEmptyField() { rejected("$base&pn=") }
    @Test fun noMissingEquals() { rejected("$base&pn") }
    @Test fun noRepeatedEquals() { rejected("$base&pn=A=B") }
    @Test fun noTrailingAmpersand() { rejected("$base&") }
    @Test fun noMalformedPercent() { rejected("$base&pn=%xy"); rejected("$base&pn=%A") }
    @Test fun noAmbiguousPlus() { rejected("$base&pn=A+B") }
    @Test fun encodedPlusIsLiteral() { assertEquals("A+B", UpiParser.parse("$base&pn=A%2BB").name) }
    @Test fun noInvalidUtf8() { rejected("$base&pn=%C0%AF"); rejected("$base&pn=%ED%A0%80") }
    @Test fun noControlCharacters() { rejected("$base&pn=A%00B"); rejected("$base&pn=A%0AB"); rejected("$base&tn=%7F") }
    @Test fun noBidiSpoofing() { rejected("$base&pn=A%E2%80%AEB"); rejected("$base&tn=%E2%81%A6abc") }
    @Test fun noInvisibleFormatCharacters() { rejected("$base&pn=A%E2%80%8BB") }
    @Test fun noUnicodeLineSeparators() { rejected("$base&tn=A%E2%80%A8B") }
    @Test fun noVpaWhitespaceOrDelimiters() { rejected("upi://pay?pa=reader%20%40example"); rejected("upi://pay?pa=reader%26evil%40example") }
    @Test fun noRawSpaces() { rejected("$base&pn=A B") }
    @Test fun noSurroundingNameSpaces() { rejected("$base&pn=%20A"); rejected("$base&pn=A%20") }
    @Test fun noForeignCurrency() { rejected("$base&cu=USD&am=1"); rejected("$base&cu=inr") }
    @Test fun noOversizePayload() { rejected("$base&pn=" + "A".repeat(2050)) }
    @Test fun noOversizeNameOrNote() { rejected("$base&pn=" + "A".repeat(121)); rejected("$base&tn=" + "A".repeat(141)) }
    @Test fun invalidAmountsRejected() {
        listOf("0", "0.00", "-1", "+1", "01", "1.", ".5", "1.234", "1e2", "NaN", "1,000", "100000.01", "999999999999999999999999", " 1", "١").forEach {
            assertThrows("Accepted $it", InvalidQr::class.java) { UpiParser.parseAmount(it) }
        }
    }
    @Test fun everyAcceptedReviewFieldMatchesLaunchPayload() {
        val qr = UpiParser.parse("$base&pn=A%26B&tn=Invoice%20%231&am=10.5")
        val request = qr.withAmount(1050)
        val sent = UpiParser.parse(request.uri())
        assertEquals(qr.vpa, sent.vpa); assertEquals(qr.name, sent.name); assertEquals(qr.note, sent.note); assertEquals(1050L, sent.amountPaise)
    }
}
