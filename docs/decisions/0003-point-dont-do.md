# 0003: Point, don't do
Status: Accepted · 2026-10-18

**Context.** Big tech is building agents that do tasks *for* people (Gemini app automation, Siri onscreen awareness). Google Play bans non-disability apps from using the Accessibility API to autonomously plan and act ([policy](https://support.google.com/googleplay/android-developer/answer/10964491?hl=en)). Scammers use screen-control apps such as AnyDesk to drain accounts.

**Decision.** Kara Do only highlights and speaks. It never taps, types or acts, and it offers no remote control.

**Consequences.** This stays within Play policy and builds real independence and trust. It is slower than "do it for me", and that is a known risk if agents become good on cheap phones.
