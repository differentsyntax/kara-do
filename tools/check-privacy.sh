#!/usr/bin/env bash
# Fails the build if Kara Do's privacy promises are broken. Run by CI on every PR.
#   1. The built APK requests no INTERNET permission (ADR-0005), and no other permission
#      we have not reviewed.
#   2. The accessibility service stays read-only (ADR-0003).
set -euo pipefail

apk="${1:?usage: check-privacy.sh path/to/app.apk}"
config="app/src/main/res/xml/accessibility_service_config.xml"
fail=0

aapt2="$(ls -d "${ANDROID_HOME:?ANDROID_HOME not set}"/build-tools/*/aapt2 | sort -V | tail -1)"
perms="$("$aapt2" dump permissions "$apk" | grep -oE "uses-permission: name='[^']+'" | sed -E "s/.*name='([^']+)'/\1/" | sort -u)"

# Permissions the app is allowed to end up with (some come from AndroidX automatically).
allowed=(
  "io.github.differentsyntax.karado.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
)
echo "Permissions in APK:"
echo "${perms:-<none>}"
for p in $perms; do
  ok=0
  for a in "${allowed[@]}"; do [[ "$p" == "$a" ]] && ok=1; done
  if [[ $ok -eq 0 ]]; then
    echo "::error::APK requests unreviewed permission $p"
    fail=1
  fi
done

for attr in canPerformGestures canRequestFilterKeyEvents canRequestTouchExplorationMode canTakeScreenshot flagRequestTouchExplorationMode flagRequestFilterKeyEvents; do
  if grep -q "$attr" "$config"; then
    echo "::error::$config must stay read-only but contains $attr"
    fail=1
  fi
done
if ! grep -q 'android:packageNames=' "$config"; then
  echo "::error::$config must restrict packageNames so no app is watched until a guide starts"
  fail=1
fi

if grep -rn --include=*.kt -E "performAction|dispatchGesture|performGlobalAction" app/src; then
  echo "::error::Kara Do points, it never acts. Remove the calls above."
  fail=1
fi

[[ $fail -eq 0 ]] && echo "Privacy checks passed."
exit $fail
