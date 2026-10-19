# Contributing to Kara Do

Thank you for helping! The most valuable contribution is a **recipe**, or a fix to one, recorded on a real phone, often a parent's (see [ADR-0006](docs/decisions/0006-community-recipes.md)).

## Licences
- **Code** is under the [Apache License 2.0](LICENSE).
- **Recipes and fixtures** in `recipes/` are under [CC BY 4.0](recipes/LICENSE), so anyone can reuse the guides with credit.

By contributing, you agree that your contribution is licensed under these terms.

## Sign your commits (DCO)
Add a `Signed-off-by` line to each commit (`git commit -s`). It certifies the [Developer Certificate of Origin](https://developercertificate.org/): that you wrote the change or have the right to submit it.

## Before you open a pull request
- `./gradlew :core:recipes:test :core:engine:test` must pass. This includes the recipe safety checks: no steps through payment, UPI, OTP, PIN, passwords or permission prompts.
- **Never include personal information.** Recorded screens must contain only button labels and layout: no names, numbers, chats or photos.
- Hindi text follows the style in [the design](docs/design/system-design.md): everyday Hinglish, with tech words such as Bluetooth and scroll kept in English letters.
- The app must keep its promises: no internet permission, and no tapping on the user's behalf. CI checks both.
