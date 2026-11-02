# NoUPI

Two small Android handoff experiments. No ads, accounts, payment backend, bank partnership, merchant onboarding or paid API.

| App | Application ID | What it does |
| --- | --- | --- |
| NoUPI Intent | `org.noupi.intent` | Scan or paste a supported static UPI QR, review it, then choose an installed app to handle payment. |
| NoUPI USSD | `org.noupi.ussd` | Open `*99#` in the system dialler. You decide whether to dial; carrier/system menus handle the session. |

The two apps install side by side on Android 8.0/API 26 or newer. Initial APKs use **debug signing**, not production release signing. No real payment or USSD session is used in automated testing.

## Intent behavior

1. Scan a QR code with on-demand camera access, or explicitly paste a UPI link.
2. Review the exact payee UPI ID, the QR name **marked unverified**, amount and note. A QR name is not bank-verified identity.
3. If the QR omits an amount, enter it. A QR-specified amount cannot be changed here.
4. Tap **Choose payment app**. Android shows a generic chooser. Choose only an app you trust: appearing there is not certification.
5. The chosen app controls its full payment and authentication screens. No PIN-only or arbitrary P2P QR compatibility is promised.
6. On return, NoUPI always says to check the payment app. `RESULT_OK`, `SUCCESS`, cancellation and missing callbacks cannot verify whether money moved. NoUPI never automatically retries or issues a payment receipt.

Supported QR syntax is intentionally narrow: exact `upi://pay?`, lowercase `pa`, optional `pn`, `am`, `cu=INR`, `tn`. Unknown fields (including signed/dynamic/security/merchant metadata) are rejected rather than stripped. Duplicate decoded keys, malformed percent/UTF-8 encoding, raw whitespace, raw `+` (ambiguous space encoding), controls, invisible format characters, bidi controls, fragments and unsupported authorities are rejected. Percent-encoded UTF-8 names/notes are supported. Names are at most 120 UTF-16 code units; notes 140. The product amount cap is ₹100,000.00; this is not a claimed bank/UPI transaction limit. See [parser tests](app/src/test/java/org/noupi/UpiParserTest.kt).

This subset will reject many genuine merchant/signed/dynamic QR codes. Use your actual payment app for those. NoUPI performs no VPA lookup, bank identity verification, transaction verification or handler certification.

## USSD behavior

Only `ACTION_DIAL` with `Uri.fromParts("tel", "*99#", null)` is used. No call permission, automatic dial, `sendUssdRequest`, SMS, DTMF, accessibility automation, PIN handling or interactive-session emulation. QR payee/amount details are not inserted into USSD. Availability and any charges depend on SIM, bank and carrier; use the bank-linked number.

## Build locally

Use JDK 17 or 21, Android SDK platform 36 and Build Tools 36.0.0. AGP 9.1.1 and Gradle 9.3.1 are pinned; AGP's built-in Kotlin is used. Android Studio's bundled JDK 21 works. Create an untracked `local.properties` with `sdk.dir=/absolute/path/to/Android/sdk`, or set `ANDROID_HOME`.

```sh
./gradlew --no-daemon :app:testIntentDebugUnitTest :app:testUssdDebugUnitTest :app:lintIntentDebug :app:lintUssdDebug :app:assembleIntentDebug :app:assembleUssdDebug
```

Outputs:

```text
app/build/outputs/apk/intent/debug/app-intent-debug.apk
app/build/outputs/apk/ussd/debug/app-ussd-debug.apk
```

Dependency versions, wrapper distribution checksum and dependency lockfiles are tracked. The normal Gradle debug key is generated locally and is never committed. Builds are reproducible from pinned source/dependencies in the usual Gradle sense; APK bytes/signatures can differ with the local debug key and build environment. Do not distribute a future production build with a debug key.

For an isolated cache on this Mac:

```sh
export JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'
export GRADLE_USER_HOME="$PWD/.gradle-home"
./gradlew --no-daemon :app:assembleDebug
```

## Safe instrumentation

Use a disposable emulator with **no bank/payment apps**, no SIM and no real payees. The `test-handler` module is a plainly labelled simulation, has no release variant, never makes network requests and cannot move money. It is a test fixture and is not part of either delivered APK.

```sh
# First run without a payment handler: verifies the no-handler UI.
./gradlew :app:connectedIntentDebugAndroidTest :app:connectedUssdDebugAndroidTest
# Install only the debug fixture; rerun Intent tests for fake returns and missing callback.
./gradlew :test-handler:installDebug :app:connectedIntentDebugAndroidTest
```

Handler-dependent tests are skipped before fixture installation; the no-handler test is skipped afterward. USSD instrumentation intercepts the dialler Intent, so it does not dial or invoke carrier menus. Screenshot tests briefly clear screenshot protection only on fictional fixture screens. See [test report](docs/TEST_REPORT.md) and [device checklist](docs/DEVICE_TESTING.md).

## Privacy and dependencies

NoUPI has no Internet, phone, SMS, notification, contacts or accessibility permission. Intent asks for camera only when scanning; USSD has no runtime permissions. QR processing is on-device. Payment details are held in activity state for lifecycle recovery, not saved to a transaction database. Android may retain activity state temporarily. Backup is disabled and sensitive screenshots/recents previews are blocked. Explicit paste uses Android text input; no background clipboard reading.

The external app receives only the reviewed UPI fields when you tap the handoff button. Its behavior and privacy are outside NoUPI's control. The system dialler receives `*99#` in USSD. No PIN or bank credentials should ever be entered into NoUPI.

Our code is [MIT](LICENSE). [Third-party notices](THIRD_PARTY_NOTICES.md) disclose ZXing, JourneyApps, Kotlin and AndroidX licenses. Full relevant runtime license texts are also packaged in the Intent APK assets. Build/test dependencies retain their own licenses.

[Task board](TASKS.md) · [Design decisions](docs/DESIGN.md) · [Research and limits](docs/RESEARCH.md)
