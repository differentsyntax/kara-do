# Trying Kara Do on an emulator

## Option A: Android Studio (recommended)
1. Install [Android Studio](https://developer.android.com/studio) and open this repo folder. Let Gradle sync finish; Studio writes `local.properties` with your SDK path, which turns on the `:app` module.
2. **Create an emulator:** *Device Manager → Create device → Pixel 8 → a recent system image with Google APIs* (Android 14 or 15). Google APIs images include text-to-speech.
3. **Run:** pick the `app` configuration and press ▶. Kara Do opens on the emulator.
4. Go through onboarding: the trusted-helper question, the disclosure, then **Accessibility settings → Kara Do guide → On**.
5. Back in Kara Do, tap **"screen की Brightness बढ़ाइए" → "रास्ता सिखाइए"**. You should land on the home screen with a saffron ring on the Settings icon and hear the step.

## Option B: the APK from CI
Every pull request uploads a debug APK. Open the PR's **Checks → CI → Artifacts → kara-do-debug-apk**, download and unzip it, then:
```sh
adb install -r app-debug.apk
```

## Handy adb shortcuts
Turn the accessibility service on without tapping through Settings (emulator only):
```sh
adb shell settings put secure enabled_accessibility_services \
  io.github.differentsyntax.karado/io.github.differentsyntax.karado.guide.GuideService
adb shell settings put secure accessibility_enabled 1
```
To test Hindi labels, switch the emulator to Hindi in *Settings → System → Languages*.

## Things to check and report
- Does the ring land exactly on the right button on each screen?
- Does the "नीचे scroll करिए" prompt appear when Display is further down the list?
- Does the voice say the Hindi naturally, including the English words?
- Does the ring disappear if you switch to another app (for example WhatsApp)?
- Does **✖ बंद करिए** stop everything?

## Recording a real fixture (for now, by hand)
Until the in-app recorder exists, a UI dump of a real screen helps a lot:
```sh
adb shell uiautomator dump /sdcard/screen.xml && adb pull /sdcard/screen.xml
```
**Check the file for personal information before sharing it.** Remove names, numbers and messages.

## Known gaps
- On a real phone running Android 13 or newer, an APK installed from a browser or file manager may show "Restricted setting" when you turn on the accessibility service. Use *App info → ⋮ → Allow restricted settings*. Installing through Android Studio or `adb` avoids this. A Play Store install will avoid it too.
- Without a Hindi TTS voice on the device, the Hindi text may be read badly or not at all. Install one in *Settings → System → Languages → Text-to-speech*.
