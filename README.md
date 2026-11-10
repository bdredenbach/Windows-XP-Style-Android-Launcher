# Nth Desktop XP

An XP-inspired Android launcher by **An Nth Experience**. Current preview: **0.3.0**.

Android 8.0 or newer. Target SDK 36. Application ID: `com.nthexperience.desktop.xp`.
It installs alongside Vista, Seven, Eight, Ten and Eleven.

## XP visual pass — 0.3.0

The shared shell now uses tighter Luna title bars, original XP.css caption artwork,
Wine's Tahoma-compatible UI fonts, XP control states and classic blue menu highlights.
The taskbar is slimmer, its clock is one line, the notification area is lighter,
and Show Desktop sits beside Quick Launch. The two app rows still scroll and accept
reordering independently. Explicit icon size choices are retained; the default is 36dp.

Start has compact destinations, Internet/E-mail slots, and colored Log Off / Turn Off
signs. Explorer uses green navigation arrows, a separate Address line and a Search
control. Notepad has its five menus, plain editor, cursor status, Find, Word Wrap,
font sizes and real document Open/Save/Save As. The browser gains IE6-style chrome;
its optional tab strip appears when more than one tab is open. Media Player gets a
blue XP-era navigation rail and menus. Control Panel and the picture viewer also
receive the shared typography and surface treatment.

This remains an Android launcher. WebView, file permissions and device settings
continue to be managed by Android. References, asset notices and design tradeoffs
are recorded in `docs/XP_0_3_VISUAL_PASS.md` and `ASSETS.md`.

[0.3.0 verification](https://github.com/bdredenbach/Windows-XP-Style-Android-Launcher/actions/runs/42913242371)
passed 103 unit tests, 70 installed-app checks per API, and the full independent-row
suite on both Android 11 and Android 16. Sixty unit renders and 22 core captures per
API cover the six color schemes, portrait/landscape layouts and refreshed app screens.

## XP training-guide fidelity pass

0.2.0 adds the supplied 20-picture wallpaper collection, including Bliss as the
fresh-install background. Existing choices are retained, and the eleven earlier
background IDs remain unchanged for portable backups. Source and license:
`third_party/xp-wallpapers/` (the notice is also bundled inside the APK).

Display Properties now uses Themes, Desktop, Screen Saver, Appearance and Settings
tabs. Theme, background and position choices are staged until Apply or OK; Cancel
discards them. The screen saver is a real optional Android DreamService, with the
same preview inside Display Properties. Android owns its start conditions and lock
security. Display resolution and scaling lead to Android's real display settings.

Explorer's Folders button switches common tasks to a folder tree; linked folders
have working expand/collapse controls. The address is a path, and Favorites joins
the classic menu bar. Task Manager uses the XP Applications, Processes, Performance,
Networking and Users tabs, retaining real, Android-permitted metrics.

See `docs/XP_0_2_FIDELITY.md` for references, behavior and remaining refinements.

[0.2.0 verification](https://github.com/bdredenbach/Windows-XP-Style-Android-Launcher/actions/runs/42904128906)
passed all 98 unit tests, 62 core launcher checks per API and both independent-row
suites on Android 11 / API 30 and Android 16 / API 36. All 18 core captures per API
contain visible content. On the headless Android 16 image, capture falls back to
rendering the actual app window when the system compositor returns black.

## Supplied icons and taskbar handoff

0.1.1 incorporates the supplied five-icon XP set, including the two Recycle Bin states. Expanded taskbar apps can move between rows, each row scrolls independently, and Start/tray/clock stay in the original bottom row. XP now saves row choices under `xp_taskbar_rows`, migrating the earlier preview’s `seven_taskbar_rows` without losing placements. Both names are accepted during backup import; new exports use the XP name. See `docs/XP_0_1_1_HANDOFF.md`.

## First preview

- Original N XP branding in the installed app, shell and startup presentation.
- Labelled Start button; profile header, white Programs pane and pale Places pane; All Programs flyouts and app tap/hold options.
- My Documents, Recent Documents, Pictures, Music, My Computer, Control Panel, Search and Run destinations.
- Run launches installed app names and the `notepad`, `explorer`, `control`, `calc` and `taskmgr` aliases.
- Six opaque appearance schemes: Luna Blue, Olive Green, Silver, Royale, Zune and Windows Classic.
- Original procedural hill landscape, the supplied XP desktop icons and additional Nth folder artwork.
- XP File and Folder Tasks, Other Places and Details pane with real storage operations. File/Edit/View/Favorites/Tools/Help menus are visible by default.
- Labelled taskbar buttons, Quick Launch, grouped windows, independent expanded taskbar rows and fixed bottom controls inherited from the shared engine.
- Display Properties scheme selection, backgrounds, icon spacing, themes and sound preferences.
- Resizable built-in windows, browser, notes, media libraries, gallery, recycle recovery and portable `.nthdesktop` backups.
- The existing optional setup and Usage Access recovery guide, including the retained App info path after a denied Android grant.

Log Off returns to an in-launcher Welcome screen and preserves windows. Turn Off offers launcher restart, Welcome and Android home selection. These controls do not power off Android or authenticate a device user.

## Build and checks

Use Java 17 and Android SDK 36:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest :fixture:assembleDebug
adb install fixture/build/outputs/apk/debug/fixture-debug.apk
adb install app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.nthexperience.desktop.xp.test/com.nthexperience.desktop.XpSmoke
adb shell am instrument -w com.nthexperience.desktop.xp.test/com.nthexperience.desktop.XpIndependentRowsSmoke
```

`XpFidelityTest` covers wallpaper decoding/backup, staged Apply/Cancel, tab persistence and system-only screen-saver binding. `XpLauncherTest` covers restored preferences, theme/backup validation, legacy themes and program tap/hold actions on a narrow menu. `XpSmoke` exercises the installed app, Explorer, Display Properties, all six schemes in both orientations, expanded Start anchoring and backup capture. See `docs/XP_0_1_1_HANDOFF.md` for the completed 0.1.1 checks. The earlier preview record remains in `docs/XP_0_1_VALIDATION.txt`.

The [0.1.1 verification run](https://github.com/bdredenbach/Windows-XP-Style-Android-Launcher/actions/runs/42897435848) passed all 94 unit tests and both installed-app suites on Android 11 and Android 16. The native core suite completed 46 checks per platform; the row suite passed real touch, state restoration and installed Android app launch checks.

## Preview scope and next fidelity pass

This is a working first XP shell preview. Several shared utilities retain their earlier Nth layouts. The next pass should compare Explorer toolbars, browser chrome, media controls, system dialogs and the tray/calendar with recorded XP use. All Programs uses grouped flyout navigation; full pointer-hover cascading menus remain a refinement. Built-in windows run inside the launcher; installed Android apps open as ordinary Android tasks.

Optional Nth features, including extra gadgets and window tools, remain available. Fresh XP installs start with gadgets hidden. Android manages privileged settings and permissions.

## Lineage and references

Based on the owner's latest Seven source; the exact baseline is recorded in `docs/XP_0_1_VALIDATION.txt`. Legacy class names and older research/checks are retained as shared-engine lineage.

- [Microsoft: Windows XP overview, November 2001](https://learn.microsoft.com/en-us/archive/msdn-magazine/2001/november/windows-xp-overview-take-advantage-of-new-windows-xp-features-in-your-apps-today)
- [Microsoft Windows XP Design Team: Visual Guidelines, archived mirror](https://www.retrospace.net/download/WebApplications/WindowsXPDesignGuidelines/whatsnew.htm)
- [Microsoft Windows XP Reviewers Guide, archived original document](https://tech-insider.org/windows/research/acrobat/0108-b.pdf)

The procedural Nth hill artwork and N XP brand are original. The supplied XP wallpaper collection, including Bliss, is attributed in `third_party/xp-wallpapers/` and `ASSETS.md`. The five supplied XP icons retain their archive provenance in `ASSETS.md`. Other shared-engine artwork retains its existing provenance in `ASSETS.md` and notices. This independent Nth project is not affiliated with Microsoft. Windows is a Microsoft trademark.
