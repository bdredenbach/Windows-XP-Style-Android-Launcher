# Nth Desktop XP 0.2.0 — training-guide pass

## References reviewed

- User-supplied Introduction to Windows XP training deck:
  https://www.slideshare.net/slideshow/introduction-to-windows-xp/128171926
- User-supplied XP lessons:
  https://www.learningcomputer.com/microsoftwindows/windowsxp/
- Wallpaper collection:
  https://github.com/storef/windows-xp.themepack/tree/ec4fb5578d78783c5aed10f39a9917132b6d60ea
- Android screen saver lifecycle and system binding:
  https://developer.android.com/reference/android/service/dreams/DreamService

Training materials were used as design references; the deck, lesson text and
videos are not redistributed. The wallpaper source notice and revision are
recorded separately in third_party/xp-wallpapers, with the license also in assets.

## Implemented

| Area | Result |
| --- | --- |
| Wallpapers | 20 unmodified source JPEGs, offline; Bliss for fresh installs; sample-size decoding and a bounded cache |
| Compatibility | Original wallpaper IDs 0–10 preserved, new IDs appended; restored/custom wallpaper choices retained |
| Display Properties | Five horizontally scrollable property tabs; vertically scrollable content; persistent draft across tabs and restored window sessions |
| Apply/Cancel | Atomic preference changes on Apply/OK; Cancel discards the draft; preview uses the draft scheme, position and actual picture |
| Desktop tab | XP backgrounds listed first; import pictures, placement options, existing slide-show gallery and desktop customization reachable |
| Screen Saver | Original Nth XP clock/logo preview and optional system DreamService; timers stop when detached/hidden; reduced-motion support |
| Settings | Actual display metrics; system screen-saver and display settings launched for privileged options |
| Explorer | Common tasks / real linked-folder tree switching, persisted and backed up; classic Favorites menu; path address and compact status bar |
| Task Manager | XP tab set, selected-state styling and palette surface; later Services tab removed; real scoped process/device metrics retained |

The source wallpapers are mostly 800x600; Autumn is 940x705. They may look softer
on high resolution phones. Custom photos, existing Nth backgrounds and placement
choices remain available. Existing installations keep their current wallpaper;
choose Bliss in Display Properties > Desktop to switch.

## Validation

[Implementation run](https://github.com/bdredenbach/Windows-XP-Style-Android-Launcher/actions/runs/42902962756)
and [capture follow-up](https://github.com/bdredenbach/Windows-XP-Style-Android-Launcher/actions/runs/42904128906)
both passed: 98 unit tests, 62 core checks per API, and both independent-row suites
on Android 11 / API 30 and Android 16 / API 36. All 18 final core captures per API
contain visible content. The Android 16 headless compositor originally returned
black captures; the follow-up renders the actual app window in that case and
requires visible image content. This changes the harness only. Native app-window
renders and compositor screenshots are distinct forms of evidence. The native suite covers staged choices, Cancel, Apply,
folder-pane switching, backup capture, the XP Task Manager tabs and the existing
six-scheme rotation checks. Both independent taskbar-row suites remain enabled.
The core screenshot collector now reads app-private snapshots via run-as, avoiding
scoped-storage restrictions on modern Android.

## Remaining fidelity work

- Search companion layout and richer file-search filters from the lessons.
- XP-specific Notepad/WordPad menus and Save As workflows.
- XP browser, media-player, photo-filmstrip and printing treatment.
- Further system-dialog, tray/calendar, task-pane icon and pointer-hover detailing.
- Desktop Cleanup Wizard and folder-option presentation.

System-wide process control, sign-in, power-off, drivers and desktop resolution
remain Android responsibilities. Built-in Nth windows run inside the launcher;
other Android apps open as Android tasks. This release is an incremental fidelity
pass, not a claim of complete Windows XP emulation.
