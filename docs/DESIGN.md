# Design decisions

A quiet pocket tool: cloud `#F2F6FA`, navy ink `#142C3B`, teal `#006779`, secondary text `#526978`, white. Native sans text, large amount, monospaced exact payee ID. The payment details are the visual focus. Comfortable 56dp controls and a scrolling layout support small screens and larger text.

Intent: Scan → review exact VPA and unverified QR name → enter INR amount only if absent → choose an external payment app → check there. Camera access is requested only in the scanner. Paste is an explicit alternative. Help contains decoder/source/license details. USSD has its own single critical action, opening the dialler.

The normal screens contain no slogan, version badge, install commands, fake balances, or fabricated transaction history. Limits affecting the decision remain beside the action. No success receipt can be shown because no trusted payment verification exists.

Guidance applied: [Unslop UI Design](https://github.com/anima-regem/skills/blob/main/unslop-ui-design/SKILL.md). Rendered-screen verification is recorded in TEST_REPORT.md.
