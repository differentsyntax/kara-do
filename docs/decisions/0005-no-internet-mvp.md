# 0005: No INTERNET permission in the MVP
Status: Accepted · 2026-10-19 (proposed 2026-10-18)

**Context.** Trust is the biggest barrier. An app that reads the screen is exactly what scammers use. Claims like "we don't upload your data" are hard to verify. A missing permission is easy to verify.

**Decision.** The MVP declares no `android.permission.INTERNET`. Recipes ship inside the APK and are updated through Play Store updates.

**Communication.** This is advertised up front on the website and in the README, because it is the easiest promise to check.

**Consequences.** This gives the strongest verifiable privacy promise and needs no backend. Recipe fixes are only as fast as app releases. Opt-in cloud help and family mode must wait for a deliberate later decision that adds the permission.
