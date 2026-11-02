# NoUPI task board

## First two-APK milestone

- [x] Inspect GitHub ownership and existing repository before creating anything.
- [x] Create an isolated local workspace; use installed SDK/JDK, no SSH or T2.
- [x] Pin compatible Gradle/AGP toolchain, keep resource use bounded.
- [x] Define two application IDs and launcher names for side-by-side installation.
- [x] Implement strict static unsigned QR subset with unknown-field rejection.
- [x] Reject duplicate/encoded duplicate keys, bad UTF-8, controls, bidi, and ambiguous encoding.
- [x] Parse positive bounded INR amounts in integer paise; no floating point.
- [x] Match reviewed fields exactly to canonical launched fields.
- [x] Show unverified QR name, exact selectable UPI ID, amount and note.
- [x] Require explicit amount only when omitted by QR.
- [x] Add on-demand camera scanning and paste fallback.
- [x] Open generic payment chooser with narrow package visibility.
- [x] Handle no handler and launch errors without automatic retries.
- [x] Consume handoff across cancellation, callbacks and recreation.
- [x] Treat all returns as unverified, including fake SUCCESS.
- [x] USSD: ACTION_DIAL only with encoded *99#, no call permission or session automation.
- [x] Create debug-only simulated handler fixture, with no release variant.
- [x] Add parser and lifecycle state unit tests.
- [x] Compile signed debug APKs locally.
- [x] Run both flavor unit tests and Android lint.
- [ ] Exercise emulator critical path and failures — **blocked**: API 36.1 stayed offline; API 35 image is incomplete. See docs/TEST_REPORT.md.
- [ ] Inspect screenshots at normal and larger text sizes — **blocked** on functioning emulator or user device.
- [x] Inspect final manifests and verify signatures/permissions/application IDs.
- [x] Document reproducible build, dependency licenses, privacy and known limits.
- [x] Publish source and verify the remote commit; publish two APKs as the v0.1.0-debug prerelease and verify asset hashes.
- [x] Copy two versioned APKs, calculate hashes and create Library attachments.
- [x] Deliver a device checklist and clearly identify untested real-app/carrier paths.

## After user device feedback

- [ ] Confirm scan focus, camera permission recovery and rotation on physical devices.
- [ ] Record supported/unsupported QR samples without retaining personal payment data.
- [ ] Test chooser compatibility on user-selected installed payment apps without NoUPI claiming bank certification.
- [ ] Validate actual dialler presentation and carrier availability on user's device.
- [ ] Review TalkBack, display scaling, long names and Malayalam rendering on physical devices.
- [ ] Decide whether an NFC-to-Intent variant adds value after the first two variants are tested.
- [ ] Plan stable release signing, upgrade continuity and distribution separately from debug builds.

## Delivery checkpoint

First two APKs published: [0.1.0 debug](https://github.com/anima-regem/NoUPI/releases/tag/v0.1.0-debug). Build source commit: `234efdc56630ddeeb2f310896de4626a31ca0f04`. GitHub asset SHA-256 values match SHA256SUMS. Both APKs also saved to ChatGPT Library. This checkpoint stops for the user's device feedback; it does not claim a production release or runtime/visual validation.
