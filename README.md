# Kara Do (करा दो)

*"Kara do"* means "please help me get this done". It is what parents say when they ask a child for help with the phone. Kara Do is an Android app that shows older and non-tech-savvy people *where to tap, step by step*, in Hindi or English, on whatever phone and app version they have. It reads the current screen on the device, highlights the next button and says it out loud. The person always does the tapping themselves.

> Status: idea and research stage. Nothing to install yet.

## Principles
1. **No internet access at all.** The first version of the app does not even ask for the internet permission. You can check this yourself on the Play Store listing under "App permissions".
2. **Set up only with someone you trust.** Kara Do should be installed and turned on by a family member or friend you know well and who knows phones, sitting with you. **Never** install it, or turn on its permissions, because a stranger, a caller or a message asked you to.
3. **Private by default.** Screen content is processed on the phone. Nothing leaves the device unless the user explicitly says yes, every time.
4. **Teach, don't take over.** Kara Do points; it never taps, types or acts on its own. It offers no remote control.
5. **Hands off sensitive apps and steps.** Banking, UPI and payment apps are excluded, and guides stop before any payment, OTP or permission step.
6. **Works on ordinary phones.** It is built for mid-range vivo, Samsung, Oppo and Xiaomi phones, not only flagships.
7. **Open source**, so anyone can check the promises above.

## Building in public
This repo keeps a trail of the journey:
- [`docs/devlog/`](docs/devlog/): progress notes
- [`docs/decisions/`](docs/decisions/): key decisions, pivots and their reasons (ADRs)
- [`docs/research/`](docs/research/): research behind the decisions

The website is published from `docs/` with GitHub Pages.
