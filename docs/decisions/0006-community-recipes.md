# 0006: Recipes are open for community contribution
Status: Proposed · 2026-10-19

**Context.** Recipes are the long-term cost. Every brand skin and app update can break one, and no single maintainer owns all the phones. The people who already help their parents (sons, daughters, grandchildren) know these flows and have the actual phones in their hands.

**Decision.** The app and the recipes are open source. Anyone can contribute a recipe or a fix for their parent's phone, and maintainers review and approve it before it ships.

How it works:
1. **Record:** in the app, the contributor turns on "Recipe recorder" and does the task once on the phone.
2. **Redact on the phone:** before anything is saved, the recorder removes names, numbers, chat text, contacts and anything in password fields. Only button labels, IDs and layout are kept. The contributor sees exactly what will be shared.
3. **Share:** the result is a small file sent through Android's normal *Share* menu (to GitHub, email or WhatsApp). The app still needs no INTERNET permission ([ADR-0005](0005-no-internet-mvp.md)).
4. **Submit:** non-developers use a simple form, a GitHub issue template that takes an attached file. Developers open a pull request.
5. **Review gate:**
   - schema check, and an engine replay against the snapshots;
   - the safety lint (no Pay, UPI, OTP or "Allow" steps);
   - a human review of the wording and the safety;
   - for Hindi text, a native-speaker check.
6. **Ship:** only maintainers merge. Recipes reach users through signed app releases.

**Consequences.**
- *Upside:* coverage grows with the community, and families on every brand become testers.
- *Risks:*
  - privacy leaks in the snapshots, mitigated by redacting on the device and a contributor preview;
  - malicious or misleading recipes, mitigated by the review gate and the safety lint;
  - review workload, mitigated by automated checks first and trusted reviewers later.
- *Licensing (decided 2026-10-19):* code under Apache-2.0, and recipes and fixtures under CC BY 4.0, with a DCO sign-off for contributions. See [CONTRIBUTING.md](../../CONTRIBUTING.md).
