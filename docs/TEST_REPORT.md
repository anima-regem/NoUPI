# Verification — 0.1.0 debug

Build date: 2026-11-02 UTC. Local personal Mac, isolated NoUPI workspace. No SSH/T2 work and no CI runner were used. Mashi's checkout, processes and data were not touched.

## Executed checks

| Check | Result |
| --- | --- |
| Intent unit suite | 50 passed: 42 parser/serialization tests and 8 handoff-state tests |
| USSD unit suite | The same shared 50 tests passed in this flavor; 100 total test executions, 50 distinct tests |
| Intent and USSD Android lint | Both pass, 0 errors. One informational warning each: a newer Gradle exists. Pinned Gradle 9.3.1 is the documented AGP 9.1.1-compatible version. |
| APK assembly | Two signed debug APKs, versionName 0.1.0, versionCode 1 |
| Instrumentation compilation | 10 Intent tests and 2 USSD tests compile, including the separate simulated handler fixture |
| APK signature inspection | Both verify with APK Signature Scheme v2, Android Debug signer; not a production release |
| Final permissions | Intent: CAMERA only. USSD: none. Neither has INTERNET, CALL_PHONE, SMS, notification, accessibility or QUERY_ALL_PACKAGES permission. |
| Application identity | org.noupi.intent / NoUPI Intent and org.noupi.ussd / NoUPI USSD; min API 26, target/compile API 36 |
| Gradle wrapper integrity | Official Gradle 9.3.1 ZIP SHA-256 matches the pinned wrapper checksum. Java's redirected download timed out locally; curl fetched the same official distribution into the isolated cache, then the wrapper validated/used it. |

The parser suite checks valid static QR input, Unicode name/note roundtrip, exact canonical handoff fields, integer paise, amount bounds, explicit missing amount and immutable QR amount. It rejects duplicate/encoded duplicate keys, ambiguous plus encoding, invalid percent/UTF-8, controls/bidi/format characters, unknown/security/dynamic fields, fragments, authority/path variations, invalid VPAs, unsupported currencies and oversized payloads. Handoff-state tests cover one deliberate launch, no handler, cancellation, fake success, missing callback, lifecycle restore and no automatic retries.

## Runtime checks blocked locally

**No emulator instrumentation test executed, and no rendered screenshot was produced.** The disposable API 36.1 ARM64 emulator (36.2.12, one core, 1536 MB) remained `offline` and logged `qemu_mprotect__osdep: mprotect failed: Permission denied`. It was stopped. The apparent installed API 35 image directory was empty and avdmanager rejected it as an invalid package. No new system image was downloaded, and no host security setting was changed.

Consequently, camera scan/permission recovery, actual chooser behavior, fake-handler roundtrips, Activity UI lifecycle behavior, dialler presentation, large-text layout and TalkBack remain **unverified at runtime**. The tests for those flows are present and compiled; that is not a substitute for execution. Screenshot tests clear FLAG_SECURE only on fictional test screens. Normal app code always sets it.

No actual UPI app transaction, real-money payment, call, PIN entry or live USSD session was performed. Compatibility with real payment apps, QR populations, banks, SIMs and carriers is not established. Follow [DEVICE_TESTING.md](DEVICE_TESTING.md) for the next checks; basic validation stops before any transaction submission or dial action.

## Evidence

- [Unit-test counts](evidence/unit-tests.json)
- [Final build log](evidence/build.log)
- [Intent signature](evidence/intent-signature.txt), [USSD signature](evidence/ussd-signature.txt)
- [APK permissions and identities](evidence/apk-inspection.txt)
- [APK SHA-256 sums](../SHA256SUMS)

Screenshots and runtime pass claims are deliberately absent until a functioning emulator or user device provides them.
