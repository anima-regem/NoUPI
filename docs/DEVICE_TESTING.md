# Device checklist — 0.1.0 debug

Install both APKs; verify separate Intent and USSD launcher names. These are development builds. Record device model, Android version, app names/versions selected, and the result. Avoid sharing real VPAs, balances, phone numbers, PINs or transaction IDs in screenshots/logs.

## NoUPI Intent

- [ ] Camera permission is requested only after Scan QR code → Allow camera.
- [ ] Deny camera; return and use Paste a UPI link. Reopen camera after enabling permission in Android settings.
- [ ] Scan a **fictional fixture QR** or paste `upi://pay?pa=reader%40example&pn=Test%20payee&am=12.30&cu=INR&tn=Tea` and stop at review. This is not a real payee and must never be submitted as a payment.
- [ ] Review shows `reader@example`, Test payee labelled unverified, ₹12.30 and Tea exactly. A QR name is never presented as verified bank identity.
- [ ] Remove `am` from the fixture; confirm an amount is required. Invalid, negative, over-cap, scientific notation and >2 decimal amounts are rejected.
- [ ] Append `&pa=other%40example` or `&sign=abc`; confirm rejection and a recoverable explanation.
- [ ] Rotate, background/resume and enlarge text; review/typed amount remain usable without launching anything.
- [ ] With no compatible handler, tapping Choose payment app shows recovery.
- [ ] On a disposable emulator only, install the labelled debug test handler and exercise fake SUCCESS, cancellation and missing callback. Every return remains unverified and no automatic retry appears.
- [ ] On your physical device, inspect whether the chooser lists the payment app you expect. Stop before submitting any transaction. NoUPI cannot certify listed apps or promise their exact screen flow.
- [ ] Use TalkBack: labels, exact UPI ID, amount, errors and the handoff button are understandable. Check Malayalam/long-name wrapping and large display size.

## NoUPI USSD

- [ ] Launching/reopening/rotating does not dial or start a session.
- [ ] Open dialler shows `*99#` intact (the # is encoded in the tel URI, not treated as a URI fragment). Stop before dialing during basic checks.
- [ ] Back returns cleanly. No PIN, bank credentials, call/SMS permissions or fake banking menu appear in NoUPI.
- [ ] Carrier/bank-linked SIM availability remains a user-device check. The app does not copy QR data into USSD and cannot read or control menus.

No automated real-money, real UPI-app transaction, real call or live USSD test has been performed. A future actual bank transaction is the user's separate decision in their trusted app/system interface, not a required NoUPI validation step.
