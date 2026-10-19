# 0001: Guide from the live screen, not a screenshot database
Status: Accepted · 2026-10-18

**Context.** The first idea was a database of screenshots and videos for each device and software version. But the combinations multiply: Android versions × phone brand skins (One UI, Funtouch/OriginOS, ColorOS, HyperOS) × app versions that update weekly. Such a database would always be stale, which is the very problem we're solving.

**Decision.** Read the screen the user is on right now (through the Android Accessibility API) and highlight the next element to tap. Store only short **recipes** per task and app (for example "Spotify: search → type song → first result") as hints. The live screen decides where things actually are.

**Consequences.** This survives app updates. It needs good matching of the screen against the recipe hints, and apps that label their buttons poorly will need a fallback.
