#!/usr/bin/env bash
set -euo pipefail
pkg=com.nthexperience.desktop.xp
collect() {
  mkdir -p screenshots
  adb pull "/sdcard/Android/data/$pkg/files/xp-verification" screenshots/ >/dev/null 2>&1 || true
  for name in 'xp-independent-rows-luna-blue.png' 'xp-independent-rows-silver.png' 'xp-independent-rows-windows-classic.png' 'xp-independent-rows-failure.png'; do
    if adb shell run-as "$pkg" test -f "files/$name"; then
      adb exec-out run-as "$pkg" cat "files/$name" > "screenshots/$name" || true
    fi
  done
  adb exec-out screencap -p > screenshots/device.png || true
  adb logcat -d -s AndroidRuntime:E XpSmoke:E > xp-logcat.txt || true
  adb shell dumpsys activity activities > xp-activities.txt || true
}
trap collect EXIT
timeout -k 3s 120s adb install --no-streaming -r build-fixture/fixture-debug.apk
timeout -k 3s 120s adb install --no-streaming -r build-apks/debug/app-debug.apk
timeout -k 3s 120s adb install --no-streaming -r build-apks/androidTest/debug/app-debug-androidTest.apk
result=0
for entry in 'XpSmoke:xp-native.txt' 'XpIndependentRowsSmoke:xp-rows.txt'; do
  runner=${entry%%:*}; log=${entry#*:}
  timeout -k 2s 15s adb shell am force-stop "$pkg"
  timeout -k 2s 15s adb shell wm size reset
  timeout -k 2s 15s adb shell pm clear "$pkg"
  timeout -k 2s 15s adb shell input keyevent KEYCODE_WAKEUP
  timeout -k 2s 15s adb shell wm dismiss-keyguard
  timeout -k 3s 300s adb shell am instrument -w "$pkg.test/com.nthexperience.desktop.$runner" | tee "$log"
  grep -q 'PASS:' "$log" || result=1
  if grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$log"; then result=1; fi
  collect
done
exit "$result"
