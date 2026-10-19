# 0004: On-device first, optional cloud
Status: Accepted · 2026-10-18

**Context.** Two options were considered: (a) build on top of Gemini, or (b) lead with privacy, so that you don't have to hand your screen to any cloud. Trust was the biggest adoption barrier in every piece of research. There is no public hook into Gemini Live's screen sharing, so "building on Gemini" would mean sending screenshots to a cloud API.

**Decision.** Privacy is the project's identity. The design has three layers:
1. **No AI:** match recipe hints against the screen's accessibility text. This is the MVP and works on any phone.
2. **On-device model:** a small language model running on the phone, for when a button moved or was renamed.
3. **Optional cloud help:** off by default, asked for every time, with personal details redacted. It is provider-agnostic, behind a single interface.

**Consequences.** This makes a simple, credible promise. Small on-device models on budget phones are unproven and need early benchmarking.
