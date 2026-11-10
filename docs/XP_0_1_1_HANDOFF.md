# XP 0.1.1 — supplied icons and independent taskbar rows

The owner supplied `Nth-Desktop-Taskbar-Rows-Handoff.zip` and `windows-xp-desktop.zip`, and authorized publishing to `bdredenbach/Windows-XP-Style-Android-Launcher`.

The taskbar archive's 38 manifest entries were checked against their SHA-256 digests. Its Eleven-only patch was not applied. XP's Seven-derived engine already had the independent renderer, row-specific scrolling, cross-row drag/drop, keyboard/accessibility row moves, pressed-view protection and lower-row control reservations described in the handoff. The work here preserves that implementation and verifies XP's larger Start button and labelled default, rather than importing Eleven's layout or branding.

## Changes

- Use all five supplied ICOs for My Computer, Network, Documents and both Recycle Bin states. Keep original ICOs, the provided README and hashes under `third_party/xp-desktop-icons/`.
- Decode embedded icon sizes to transparent PNGs. Shared icon surfaces choose an available native size; the tray network button shares the same asset.
- Give XP its own validated `xp_taskbar_rows` map. On upgrade, preserve the earlier preview's `seven_taskbar_rows` choices. Earlier XP/Seven backups are mapped to the XP key. Newly exported backups contain the XP key.
- Keep explicit row choices stronger than the running/idle default. Preserve independent offsets, full upper-row width and measured lower-row reservations for Start/tray/clock.
- Add XP-specific native row regression in Blue, Silver and Classic, using real swipe/drag input, opposite-row geometry, wheel and edge behavior, interrupted/empty-row drags, launch/reopen, rotation, backup restore, static fitting, keyboard and accessible moves.
- Use the stable emulator/image pairing from the Seven foundation in the native Android 11/16 workflow. Test APKs may be CI-signed; the downloadable user APK retains the 0.1.0 certificate.

## Local validation

Java 17 / Gradle 8.13 / compile and target SDK 36; minimum SDK 26.

- Application and instrumentation APKs build.
- 94 unit tests pass, including actual bitmap rendering, legacy row migration, strict backup validation, independent row geometry and Start tap/hold behavior.
- 36 native-graphics render images cover all six themes and Start/Explorer/Display in portrait and landscape.
- APK v2 signature verifies. Certificate SHA-256 remains `557675b2401275104bf87c13c332f28cfd907df7898d979e952b7e4daa2481e2`.
- `git diff --check` passes.

The GitHub workflow retains the separate installed-app and row suites and their native results. This document records local checks; workflow run artifacts provide subsequent emulator evidence. The earlier software-only emulator installation failure is not treated as an XP application test result.

## Input scope

0.1.1 implements the attached icon set and taskbar handoff. It does not claim the separate, broader XP fidelity pass is complete. Those remaining Explorer/browser/media/Task Manager/dialog refinements stay in the README.
