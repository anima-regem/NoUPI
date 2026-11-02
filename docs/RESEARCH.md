# Research and boundaries

Reviewed 2026-11-02. These documents support the handoff design; they do not prove compatibility with any particular installed payment app or arbitrary QR code.

- [Google Pay India: in-app payments](https://developers.google.com/pay/india/api/android/in-app-payments): demonstrates payment URI flow, not authentication-only screens, and calls for payment verification through the payment service provider. NoUPI has no payment service provider/backend and therefore makes no success claim.
- [Google Pay India: overview](https://developers.google.com/pay/india/api/android/overview): merchant integration prerequisites are not evidence that a generic P2P QR will work in every app. NoUPI does not integrate Google Pay's merchant SDK.
- [Android: package visibility declarations](https://developer.android.com/training/package-visibility/declaring): supports a narrowly scoped `queries/intent` declaration. NoUPI does not use `QUERY_ALL_PACKAGES`.
- [Android: unsafe deep links](https://developer.android.com/privacy-and-security/risks/unsafe-use-of-deeplinks): motivates strict validation of untrusted QR text. NoUPI never uses `Intent.parseUri` or follows HTTP redirects. A generic custom-scheme handler can be untrusted; the user must choose an app they trust.
- [Android Intent ACTION_DIAL](https://developer.android.com/reference/android/content/Intent#ACTION_DIAL): opens the dialler for user action. NoUPI does not use ACTION_CALL.
- [PNB: USSD](https://pnb.bank.in/USSD.html): describes `*99#` from a bank-registered mobile number and system interactive menus. NoUPI makes no claim of SIM/bank/carrier availability or session automation.
- [Android TelephonyManager](https://developer.android.com/reference/android/telephony/TelephonyManager#sendUssdRequest(java.lang.String,%20android.telephony.TelephonyManager.UssdResponseCallback,%20android.os.Handler)): a request callback is not a documented general interactive USSD-session API. NoUPI does not invoke it.
- [AGP 9.1 compatibility](https://developer.android.com/build/releases/agp-9-1-0-release-notes): Gradle 9.3.1, Build Tools 36.0.0, JDK 17 minimum. This build uses the installed Android Studio JDK 21 and platform 36.
- [Built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin): AGP 9 enables built-in Kotlin, avoiding a redundant Android Kotlin plugin.
- [ZXing Android Embedded](https://github.com/journeyapps/zxing-android-embedded), [ZXing Core](https://github.com/zxing/zxing): open-source decoder/camera integration, Apache 2.0. No hosted scanner API or ML service.

## Deliberate limits

A small unsigned static field allowlist is a product restriction, not a complete UPI specification implementation. Unknown or security-bearing fields trigger rejection. No field is silently dropped from an accepted QR. All accepted review values survive canonical URI serialization; amount is rendered with exactly two decimals and currency is fixed to INR.

NoUPI is a frontend handoff experiment, not a TPAP, bank, wallet, payment gateway or bank-authentication replacement. No bank SDK, Lite wallet or merchant integration is included. NFC is deferred until the first two variants receive device feedback.
